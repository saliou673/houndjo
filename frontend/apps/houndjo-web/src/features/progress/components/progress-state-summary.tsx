"use client";

import { useGetProgressState } from "@api-client";
import { AlertTriangle } from "lucide-react";
import { useTranslations } from "next-intl";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

type ProgressStateSummaryProps = {
    studentId: number;
    courseId: number;
};

export function ProgressStateSummary({ studentId, courseId }: ProgressStateSummaryProps) {
    const t = useTranslations("Progress.state");
    const { data, isLoading, isError } = useGetProgressState(studentId, { courseId });

    return (
        <Card>
            <CardHeader>
                <CardTitle className="text-base">{t("title")}</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3 text-sm">
                {isLoading && (
                    <p className="text-muted-foreground">{t("loading")}</p>
                )}
                {isError && (
                    <p className="text-destructive">{t("errorFallback")}</p>
                )}
                {data && (
                    <>
                        <div className="grid grid-cols-2 gap-3">
                            <div>
                                <p className="font-medium">{t("sabak")}</p>
                                <p className="text-muted-foreground">
                                    {data.sabak
                                        ? t("portionLabel", {
                                              fromSurah: data.sabak.fromSurah ?? 0,
                                              fromVerse: data.sabak.fromVerse ?? 0,
                                              toSurah: data.sabak.toSurah ?? 0,
                                              toVerse: data.sabak.toVerse ?? 0,
                                          })
                                        : t("notRecorded")}
                                </p>
                            </div>
                            <div>
                                <p className="font-medium">{t("sabqi")}</p>
                                <p className="text-muted-foreground">
                                    {data.sabqi
                                        ? t("portionLabel", {
                                              fromSurah: data.sabqi.fromSurah ?? 0,
                                              fromVerse: data.sabqi.fromVerse ?? 0,
                                              toSurah: data.sabqi.toSurah ?? 0,
                                              toVerse: data.sabqi.toVerse ?? 0,
                                          })
                                        : t("notRecorded")}
                                </p>
                            </div>
                        </div>
                        <p className="text-muted-foreground">
                            {t("coveredJuz", { count: data.coveredJuz?.length ?? 0 })}
                        </p>
                        {data.stalePortions && data.stalePortions.length > 0 ? (
                            <div className="space-y-1.5 rounded-md border border-destructive/40 bg-destructive/5 p-3">
                                <p className="flex items-center gap-1.5 font-medium text-destructive">
                                    <AlertTriangle size={16} />
                                    {t("staleTitle")}
                                </p>
                                <div className="flex flex-wrap gap-1.5">
                                    {data.stalePortions.map((portion) => (
                                        <Badge
                                            key={portion.juz}
                                            variant="destructive"
                                        >
                                            {t("staleJuz", {
                                                juz: portion.juz ?? 0,
                                                days: portion.daysSince ?? 0,
                                            })}
                                        </Badge>
                                    ))}
                                </div>
                            </div>
                        ) : (
                            <p className="text-muted-foreground">{t("noStale")}</p>
                        )}
                    </>
                )}
            </CardContent>
        </Card>
    );
}
