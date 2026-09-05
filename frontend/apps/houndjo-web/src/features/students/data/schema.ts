import { type Student, type StudentGenderEnumKey } from "@api-client";

export type StudentRow = {
    id: number;
    firstName: string;
    lastName: string;
    birthDate: string | null;
    gender: StudentGenderEnumKey | null;
    guardianName: string | null;
    guardianPhone: string | null;
    creationDate: string | null;
};

export function mapStudentToRow(student: Student): StudentRow {
    return {
        id: student.id ?? 0,
        firstName: student.firstName ?? "",
        lastName: student.lastName ?? "",
        birthDate: student.birthDate ?? null,
        gender: student.gender ?? null,
        guardianName: student.guardianName ?? null,
        guardianPhone: student.guardianPhone ?? null,
        creationDate: student.creationDate ?? null,
    };
}
