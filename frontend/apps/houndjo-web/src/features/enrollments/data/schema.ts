import { type Enrollment, type EnrollmentStatusEnumKey } from "@api-client";

export type EnrollmentRow = {
    id: number;
    studentId: number;
    studentName: string;
    classId: number;
    className: string;
    courseIds: number[];
    status: EnrollmentStatusEnumKey;
    startDate: string | null;
    endDate: string | null;
};

export function mapEnrollmentToRow(enrollment: Enrollment): EnrollmentRow {
    return {
        id: enrollment.id ?? 0,
        studentId: enrollment.studentId ?? 0,
        studentName: enrollment.studentName ?? "",
        classId: enrollment.classId ?? 0,
        className: enrollment.className ?? "",
        courseIds: enrollment.courseIds ?? [],
        status: enrollment.status ?? "ACTIVE",
        startDate: enrollment.startDate ?? null,
        endDate: enrollment.endDate ?? null,
    };
}
