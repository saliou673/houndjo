import {
    type Class,
    type Course,
    type CourseQuranModeEnumKey,
    type CourseTypeEnumKey,
} from "@api-client";

export type ClassRow = {
    id: number;
    name: string;
    description: string | null;
    displayOrder: number;
    courseCount: number;
    creationDate: string | null;
};

export function mapClassToRow(schoolClass: Class): ClassRow {
    return {
        id: schoolClass.id ?? 0,
        name: schoolClass.name ?? "",
        description: schoolClass.description ?? null,
        displayOrder: schoolClass.displayOrder ?? 0,
        courseCount: schoolClass.courseCount ?? 0,
        creationDate: schoolClass.creationDate ?? null,
    };
}

export type CourseRow = {
    id: number;
    classId: number;
    name: string;
    type: CourseTypeEnumKey;
    description: string | null;
    qaidaLessons: string[];
    quranMode: CourseQuranModeEnumKey | null;
    quranScopeFromJuz: number | null;
    quranScopeToJuz: number | null;
    bookTitle: string | null;
    bookTotalChapters: number | null;
    bookTotalPages: number | null;
    creationDate: string | null;
};

export function mapCourseToRow(course: Course): CourseRow {
    return {
        id: course.id ?? 0,
        classId: course.classId ?? 0,
        name: course.name ?? "",
        type: course.type ?? "QAIDA",
        description: course.description ?? null,
        qaidaLessons: course.qaidaLessons ?? [],
        quranMode: course.quranMode ?? null,
        quranScopeFromJuz: course.quranScope?.fromJuz ?? null,
        quranScopeToJuz: course.quranScope?.toJuz ?? null,
        bookTitle: course.bookTitle ?? null,
        bookTotalChapters: course.bookTotalChapters ?? null,
        bookTotalPages: course.bookTotalPages ?? null,
        creationDate: course.creationDate ?? null,
    };
}
