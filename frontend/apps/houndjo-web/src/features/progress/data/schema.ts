import { z } from "zod";
import { type useTranslations } from "next-intl";

export const RATING_OPTIONS = ["WEAK", "FAIR", "GOOD", "EXCELLENT"] as const;

export function createQuranFlowSchema(t: ReturnType<typeof useTranslations>) {
    return z.object({
        fromSurah: z.number().int().min(1).max(114, { message: t("surahRequired") }),
        fromVerse: z.number().int().min(1, { message: t("verseRequired") }),
        toSurah: z.number().int().min(1).max(114, { message: t("surahRequired") }),
        toVerse: z.number().int().min(1, { message: t("verseRequired") }),
        errorCount: z.number().int().min(0),
        fluency: z.enum(RATING_OPTIONS, { message: t("fluencyRequired") }),
        tajweed: z.enum(RATING_OPTIONS).optional(),
        note: z.string().max(2000).optional(),
    });
}

export type QuranFlowForm = z.infer<ReturnType<typeof createQuranFlowSchema>>;

export function createLessonFlowSchema(t: ReturnType<typeof useTranslations>) {
    return z.object({
        lessonId: z.number().int().min(0, { message: t("lessonRequired") }),
        errorCount: z.number().int().min(0),
        fluency: z.enum(RATING_OPTIONS, { message: t("fluencyRequired") }),
        tajweed: z.enum(RATING_OPTIONS).optional(),
        note: z.string().max(2000).optional(),
    });
}

export type LessonFlowForm = z.infer<ReturnType<typeof createLessonFlowSchema>>;

export function createChapterFlowSchema(t: ReturnType<typeof useTranslations>) {
    return z.object({
        chapterNo: z.number().int().min(1, { message: t("chapterRequired") }),
        pageNo: z.number().int().min(1, { message: t("pageRequired") }),
        errorCount: z.number().int().min(0),
        fluency: z.enum(RATING_OPTIONS, { message: t("fluencyRequired") }),
        tajweed: z.enum(RATING_OPTIONS).optional(),
        note: z.string().max(2000).optional(),
    });
}

export type ChapterFlowForm = z.infer<ReturnType<typeof createChapterFlowSchema>>;
