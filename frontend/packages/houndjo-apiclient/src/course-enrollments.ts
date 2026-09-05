import { getEnrollments } from "./gen/client/enrollment-management/getEnrollments";
import {
    getEnrollmentsQueryKey,
    useGetEnrollments,
} from "./gen/react-query/enrollment-management/useGetEnrollments";

/** Load all active enrollments before filtering: a course's students may occur on any page. */
export async function getActiveCourseEnrollments(
    classId: number,
    courseId: number,
    signal?: AbortSignal
) {
    const items = [];
    let page = 0;
    let totalPages = 1;
    do {
        const result = await getEnrollments(
            { classId, status: "ACTIVE", pageable: { page, size: 100 } },
            undefined,
            { signal }
        );
        items.push(...result.items.filter((enrollment) => enrollment.courseIds?.includes(courseId)));
        totalPages = result.totalPages ?? 1;
        page += 1;
    } while (page < totalPages);
    return { items, page: 0, size: items.length, totalPages: 1, totalItems: items.length };
}

export function useActiveCourseEnrollments(classId: number, courseId: number, enabled = true) {
    const params = { classId, status: "ACTIVE" as const, pageable: { page: 0, size: 100 } };
    return useGetEnrollments(params, undefined, {
        query: {
            enabled,
            queryKey: [...getEnrollmentsQueryKey(params), { courseId, allPages: true }],
            queryFn: ({ signal }) => getActiveCourseEnrollments(classId, courseId, signal),
        },
    });
}
