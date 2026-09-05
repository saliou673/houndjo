const assert = require("node:assert/strict");
const fs = require("node:fs");
const path = require("node:path");
const test = require("node:test");
const vm = require("node:vm");
const ts = require("typescript");

// Run the TypeScript module with its generated transport mocked; no HTTP or React mount required.
function load(fetchPage) {
    const source = fs.readFileSync(path.join(__dirname, "../src/course-enrollments.ts"), "utf8");
    const { outputText } = ts.transpileModule(source, {
        compilerOptions: { module: ts.ModuleKind.CommonJS, target: ts.ScriptTarget.ES2020 },
    });
    const exports = {};
    vm.runInNewContext(outputText, {
        exports,
        require: () => ({ getEnrollments: fetchPage }),
    });
    return exports.getActiveCourseEnrollments;
}

test("includes course students beyond the first 100 class enrollments", async () => {
    const pages = [];
    const loadStudents = load(async (params) => {
        pages.push(params.pageable.page);
        assert.equal(params.classId, 3);
        assert.equal(params.status, "ACTIVE");
        return {
            totalPages: 2,
            items: params.pageable.page === 0
                ? Array.from({ length: 100 }, (_, id) => ({ studentId: id, courseIds: [8] }))
                : [{ studentId: 101, courseIds: [9] }],
        };
    });
    const result = await loadStudents(3, 9);
    assert.deepEqual(pages, [0, 1]);
    assert.equal(result.items.length, 1);
    assert.equal(result.items[0].studentId, 101);
});

test("rejects a failed later page instead of returning an incomplete or empty list", async () => {
    const failure = new Error("network unavailable");
    const loadStudents = load(async ({ pageable }) => {
        if (pageable.page === 1) throw failure;
        return { items: [], totalPages: 2 };
    });
    await assert.rejects(loadStudents(3, 9), (error) => error === failure);
});

test("forwards cancellation to every page request", async () => {
    const signal = new AbortController().signal;
    let calls = 0;
    const loadStudents = load(async (_params, _headers, config) => {
        assert.equal(config.signal, signal);
        calls += 1;
        return { items: [], totalPages: 2 };
    });
    await loadStudents(3, 9, signal);
    assert.equal(calls, 2);
});

test("handles an empty class without requesting another page", async () => {
    let calls = 0;
    const result = await load(async () => {
        calls += 1;
        return { items: [], totalPages: 0 };
    })(3, 9);
    assert.equal(calls, 1);
    assert.equal(result.items.length, 0);
});
