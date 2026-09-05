/* global __dirname */
const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const test = require('node:test');
const path = require('node:path');
const ts = require('typescript');
const source = fs.readFileSync(
  path.join(__dirname, '../src/app/(app)/classes/courses/sessions/attendance.tsx'),
  'utf8',
);
function harness(
  existing,
  { failed = false, ready = true, students = [{ studentId: 1, studentName: 'Student' }] } = {},
) {
  let states = [],
    cursor = 0,
    pending = false,
    payload,
    finish,
    toast = false,
    requests = 0;
  const invalidations = [];
  const jsx = (type, props) => ({ type, props });
  const api = {
    useActiveCourseEnrollments: () => ({
      data: { items: students },
      isLoading: false,
      isError: false,
      isSuccess: true,
    }),
    useGetAttendance: () => ({
      data: existing,
      isLoading: false,
      isError: failed,
      isSuccess: ready && !failed,
    }),
    useRecordBulkAttendance: () => ({
      isPending: pending,
      mutateAsync: (data) => {
        payload = data;
        requests++;
        pending = true;
        return new Promise((resolve) => {
          finish = () => {
            pending = false;
            resolve([]);
          };
        });
      },
    }),
    getAttendanceQueryKey: (id) => [id],
  };
  const exports = {};
  const requireMock = (name) => {
    if (name === 'react/jsx-runtime') return { jsx, jsxs: jsx, Fragment: 'Fragment' };
    if (name === 'react')
      return {
        useMemo: (fn) => fn(),
        useState(initial) {
          const i = cursor++;
          if (!(i in states)) states[i] = initial;
          return [
            states[i],
            (v) => {
              states[i] = typeof v === 'function' ? v(states[i]) : v;
            },
          ];
        },
      };
    if (name === 'react-native') return { StyleSheet: { create: (x) => x } };
    if (name === 'react-i18next') return { useTranslation: () => ({ t: (x) => x }) };
    if (name === '@tanstack/react-query')
      return {
        useQueryClient: () => ({
          invalidateQueries: async (options) => {
            invalidations.push(options.queryKey);
          },
        }),
      };
    if (name === 'expo-router')
      return {
        Stack: { Screen: 'Screen' },
        useLocalSearchParams: () => ({ classId: '1', courseId: '1', sessionId: '1' }),
      };
    if (name === '@api-client') return api;
    if (name === '@/hooks/use-theme') return { useTheme: () => ({}) };
    if (name === '@/constants/theme') return { Spacing: {} };
    if (name === '@/components/toast/toast-store')
      return {
        showToast: () => {
          toast = true;
        },
      };
    if (name === 'axios') return { AxiosError: Error };
    return new Proxy({}, { get: (_, key) => key });
  };
  vm.runInNewContext(
    ts.transpileModule(source, {
      compilerOptions: {
        module: ts.ModuleKind.CommonJS,
        jsx: ts.JsxEmit.ReactJSX,
        target: ts.ScriptTarget.ES2020,
      },
    }).outputText,
    { exports, require: requireMock },
  );
  function render() {
    cursor = 0;
    return exports.default();
  }
  function find(node, type) {
    if (!node) return;
    if (Array.isArray(node)) {
      for (const item of node) {
        const r = find(item, type);
        if (r) return r;
      }
    } else if (typeof node === 'object') {
      if (node.type === type) return node;
      return find(node.props?.children, type);
    }
  }
  return {
    render,
    find,
    invalidations,
    get requests() {
      return requests;
    },
    get payload() {
      return payload;
    },
    finish: () => finish(),
    get toast() {
      return toast;
    },
  };
}

test('failed attendance GET shows an error and no editable roster or save action', () => {
  for (const existing of [undefined, [{ studentId: 1, status: 'ABSENT_UNJUSTIFIED' }]]) {
    const result = harness(existing, { failed: true });
    const tree = result.render();
    assert.equal(result.find(tree, 'SubmitButton'), undefined);
    assert.equal(result.find(tree, 'RadioGroup'), undefined);
    assert.match(JSON.stringify(tree), /errorFallback/);
    assert.equal(result.requests, 0);
  }
});
test('attendance must load successfully before offering PRESENT defaults', () => {
  const result = harness(undefined, { ready: false });
  const tree = result.render();
  assert.ok(result.find(tree, 'Spinner'));
  assert.equal(result.find(tree, 'SubmitButton'), undefined);
});
test('save preserves existing reasons and refreshes attendance history', async () => {
  const result = harness([
    { studentId: 1, status: 'ABSENT_JUSTIFIED', reason: 'Medical appointment' },
  ]);
  const saving = result.find(result.render(), 'SubmitButton').props.onPress();
  assert.equal(result.payload.data.entries[0].status, 'ABSENT_JUSTIFIED');
  assert.equal(result.payload.data.entries[0].reason, 'Medical appointment');
  result.finish();
  await saving;
  assert.equal(result.toast, true);
  assert.ok(result.invalidations.some((key) => key[0] === 1));
  assert.ok(
    result.invalidations.some((key) => key[0].url === '/api/v1/students/:studentId/attendance'),
  );
});
test('pending save blocks status edits and duplicate submit callbacks', async () => {
  const result = harness([{ studentId: 1, status: 'PRESENT' }]);
  result.find(result.render(), 'RadioGroup').props.onValueChange('ABSENT_UNJUSTIFIED');
  const saving = result.find(result.render(), 'SubmitButton').props.onPress();
  const pendingTree = result.render();
  const radio = result.find(pendingTree, 'RadioGroup');
  assert.equal(radio.props.disabled, true);
  radio.props.onValueChange('PRESENT');
  await result.find(pendingTree, 'SubmitButton').props.onPress();
  assert.equal(result.requests, 1);
  result.finish();
  await saving;
  assert.equal(result.payload.data.entries[0].status, 'ABSENT_UNJUSTIFIED');
  assert.equal(result.find(result.render(), 'RadioGroup').props.value, 'ABSENT_UNJUSTIFIED');
});
test('a successfully loaded empty attendance record allows a new PRESENT entry', async () => {
  const result = harness([]);
  const saving = result.find(result.render(), 'SubmitButton').props.onPress();
  assert.equal(result.payload.data.entries[0].studentId, 1);
  assert.equal(result.payload.data.entries[0].status, 'PRESENT');
  result.finish();
  await saving;
});
test('missing, invalid or duplicate roster ids prevent partial or ambiguous saves', () => {
  for (const students of [[{}], [{ studentId: 0 }], [{ studentId: 1 }, { studentId: 1 }]]) {
    const result = harness([], { students });
    const tree = result.render();
    assert.equal(result.find(tree, 'SubmitButton'), undefined);
    assert.match(JSON.stringify(tree), /errorFallback/);
  }
});
