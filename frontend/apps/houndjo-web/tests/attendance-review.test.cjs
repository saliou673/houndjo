const test = require("node:test");
const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const vm = require("node:vm");
const ts = require("typescript");
function load(file, overrides = {}) {
    const exports = {};
    const source = fs.readFileSync(
        path.join(__dirname, "../src/features/attendance/", file),
        "utf8"
    );
    const jsx = (type, props) => ({ type, props });
    const mocks = {
        "react/jsx-runtime": { jsx, jsxs: jsx },
        react: { useMemo: (fn) => fn(), memo: (fn) => fn },
        "next-intl": { useTranslations: () => (key) => key },
        "@hookform/resolvers/zod": { zodResolver: () => undefined },
        "../data/schema": { createRollCallSchema: () => undefined },
        sonner: { toast: { success() {} } },
        ...overrides,
    };
    vm.runInNewContext(
        ts.transpileModule(source, {
            compilerOptions: {
                module: ts.ModuleKind.CommonJS,
                jsx: ts.JsxEmit.ReactJSX,
                target: ts.ScriptTarget.ES2020,
            },
        }).outputText,
        {
            exports,
            require: (name) =>
                mocks[name] ??
                (name === "zod"
                    ? require("zod")
                    : new Proxy({}, { get: (_, key) => key })),
        }
    );
    return exports;
}
function find(node, type) {
    if (!node) return;
    if (Array.isArray(node))
        return node.map((child) => find(child, type)).find(Boolean);
    if (node.type === type) return node;
    return find(node.props?.children, type);
}
function rollCall({ error = false, missingId = false } = {}) {
    const requests = [],
        invalidations = [];
    const component = load("components/attendance-roll-call-dialog.tsx", {
        "@api-client": {
            useActiveCourseEnrollments: () => ({
                isSuccess: true,
                data: {
                    items: [
                        {
                            studentId: missingId ? undefined : 1,
                            studentName: "Student",
                        },
                    ],
                },
            }),
            useGetAttendance: () => ({
                isSuccess: !error,
                isError: error,
                data: error
                    ? undefined
                    : [
                          {
                              studentId: 1,
                              status: "ABSENT_JUSTIFIED",
                              reason: "Sick",
                          },
                      ],
            }),
            useRecordBulkAttendance: () => ({
                isPending: false,
                mutateAsync: async (data) => {
                    requests.push(data);
                },
            }),
            getAttendanceQueryKey: (sessionId) => [{ sessionId }],
        },
        "@tanstack/react-query": {
            useQueryClient: () => ({
                invalidateQueries: async (options) => {
                    invalidations.push(options.queryKey);
                },
            }),
        },
        "react-hook-form": {
            useForm: ({ values }) => ({
                control: {},
                handleSubmit: (callback) => () => callback(values),
            }),
        },
    }).AttendanceRollCallDialog;
    return {
        tree: component({
            classId: 1,
            courseId: 1,
            sessionId: 1,
            sessionDate: "2026-03-02",
            open: true,
            onOpenChange() {},
        }),
        requests,
        invalidations,
    };
}
test("failed existing-attendance query prevents both button and programmatic save", async () => {
    const result = rollCall({ error: true });
    assert.equal(find(result.tree, "Button").props.disabled, true);
    await find(result.tree, "form").props.onSubmit();
    assert.equal(result.requests.length, 0);
});
test("missing roster student id cannot become a studentId=0 submission", async () => {
    const result = rollCall({ missingId: true });
    assert.equal(find(result.tree, "Button").props.disabled, true);
    await find(result.tree, "form").props.onSubmit();
    assert.equal(result.requests.length, 0);
});
test("successful save retains reason and invalidates session plus student histories", async () => {
    const result = rollCall();
    await find(result.tree, "form").props.onSubmit();
    assert.equal(result.requests[0].data.entries[0].reason, "Sick");
    assert.ok(
        result.invalidations.some(
            (key) => key[0].url === "/api/v1/students/:studentId/attendance"
        )
    );
    assert.ok(result.invalidations.some((key) => key[0].sessionId === 1));
});
test("roll-call schema enforces positive ids, known statuses and the 255-character reason bound", () => {
    const schema = load("data/schema.ts").createRollCallSchema((key) => key);
    const entry = { studentId: 1, status: "PRESENT", reason: "a".repeat(255) };
    assert.equal(schema.safeParse({ entries: { 1: entry } }).success, true);
    for (const bad of [
        { ...entry, studentId: 0 },
        { ...entry, status: "UNKNOWN" },
        { ...entry, reason: "a".repeat(256) },
    ]) {
        assert.equal(schema.safeParse({ entries: { 1: bad } }).success, false);
    }
});

test("read-only leave access renders the list without the create form or attendance query", () => {
    const Dialog = load(
        "components/student-attendance-dialog.tsx"
    ).StudentAttendanceDialog;
    const tree = Dialog({
        studentId: 1,
        studentName: "Student",
        open: true,
        canReadAttendance: false,
        canReadPermission: true,
        canCreatePermission: false,
        canUpdatePermission: false,
        onOpenChange() {},
    });
    assert.ok(find(tree, "AttendancePermissionList"));
    assert.equal(find(tree, "AttendancePermissionForm"), undefined);
    assert.equal(find(tree, "AttendanceHistorySection"), undefined);
});

test("invalid permission identifiers show an error and no approve action", () => {
    const List = load("components/attendance-permission-list.tsx", {
        "@api-client": {
            useGetAttendancePermissionsByStudent: () => ({
                data: [{ status: "PENDING" }],
                isLoading: false,
                isError: false,
            }),
            useUpdateAttendancePermissionStatus: () => ({
                mutate() {
                    throw Error("must not patch");
                },
                isPending: false,
            }),
        },
        "../data/policy": {
            hasValidId: (value) =>
                Number.isSafeInteger(value.id) && value.id > 0,
        },
        "@tanstack/react-query": { useQueryClient: () => ({}) },
    }).AttendancePermissionList;
    const tree = List({ studentId: 1, canUpdatePermission: true });
    assert.equal(find(tree, "Button"), undefined);
    assert.match(JSON.stringify(tree), /errorFallback/);
    assert.doesNotMatch(JSON.stringify(tree), /noResults/);
});
