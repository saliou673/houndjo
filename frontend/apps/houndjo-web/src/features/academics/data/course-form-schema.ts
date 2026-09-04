import { z } from "zod";
import { type useTranslations } from "next-intl";

export function createCourseFormSchema(t: ReturnType<typeof useTranslations>) {
    return z
        .object({
            name: z.string().trim().min(1, t("nameRequired")).max(150),
            type: z.enum(["QAIDA", "QURAN", "BOOK"]),
            description: z.string().optional(),
            quranMode: z.enum(["NAZIRA", "HIFZ"]).optional(),
            quranScopeFromJuz: z.number().int().min(1).max(30).optional(),
            quranScopeToJuz: z.number().int().min(1).max(30).optional(),
            bookTitle: z.string().trim().max(150).optional(),
            bookTotalChapters: z.number().int().positive().optional(),
            bookTotalPages: z.number().int().positive().optional(),
        })
        .superRefine((values, ctx) => {
            if (values.type === "QURAN") {
                if (!values.quranMode) {
                    ctx.addIssue({
                        code: z.ZodIssueCode.custom,
                        message: t("quranModeRequired"),
                        path: ["quranMode"],
                    });
                }
                if (values.quranScopeFromJuz == null) {
                    ctx.addIssue({
                        code: z.ZodIssueCode.custom,
                        message: t("quranScopeRequired"),
                        path: ["quranScopeFromJuz"],
                    });
                }
                if (values.quranScopeToJuz == null) {
                    ctx.addIssue({
                        code: z.ZodIssueCode.custom,
                        message: t("quranScopeRequired"),
                        path: ["quranScopeToJuz"],
                    });
                }
                if (
                    values.quranScopeFromJuz != null &&
                    values.quranScopeToJuz != null &&
                    values.quranScopeFromJuz > values.quranScopeToJuz
                ) {
                    ctx.addIssue({
                        code: z.ZodIssueCode.custom,
                        message: t("quranScopeInvalid"),
                        path: ["quranScopeToJuz"],
                    });
                }
            }

            if (values.type === "BOOK" && !values.bookTitle?.trim()) {
                ctx.addIssue({
                    code: z.ZodIssueCode.custom,
                    message: t("bookTitleRequired"),
                    path: ["bookTitle"],
                });
            }
        });
}

export type CourseForm = z.infer<ReturnType<typeof createCourseFormSchema>>;
