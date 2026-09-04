"use client";

import { useGetClasses, useGetStudents, type EnrollmentStatusEnumKey } from "@api-client";
import { useTranslations } from "next-intl";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";

const LIST_PAGEABLE = { page: 0, size: 100 };
const STATUSES: EnrollmentStatusEnumKey[] = ["ACTIVE", "ENDED"];
const ALL = "all";

export type EnrollmentFilters = {
    classId?: number;
    studentId?: number;
    status?: EnrollmentStatusEnumKey;
};

type EnrollmentFiltersBarProps = {
    filters: EnrollmentFilters;
    onChange: (filters: EnrollmentFilters) => void;
};

export function EnrollmentFiltersBar({
    filters,
    onChange,
}: EnrollmentFiltersBarProps) {
    const t = useTranslations("Enrollments.filters");
    const { data: classesData } = useGetClasses({ pageable: LIST_PAGEABLE });
    const { data: studentsData } = useGetStudents({ pageable: LIST_PAGEABLE });
    const classes = classesData?.items ?? [];
    const students = studentsData?.items ?? [];

    return (
        <div className="flex flex-wrap gap-2">
            <Select
                value={filters.classId != null ? String(filters.classId) : ALL}
                onValueChange={(value) =>
                    onChange({
                        ...filters,
                        classId: value === ALL ? undefined : Number(value),
                    })
                }
            >
                <SelectTrigger className="w-full sm:w-48">
                    <SelectValue placeholder={t("classPlaceholder")} />
                </SelectTrigger>
                <SelectContent>
                    <SelectItem value={ALL}>{t("allClasses")}</SelectItem>
                    {classes.map((schoolClass) => (
                        <SelectItem key={schoolClass.id} value={String(schoolClass.id)}>
                            {schoolClass.name}
                        </SelectItem>
                    ))}
                </SelectContent>
            </Select>
            <Select
                value={filters.studentId != null ? String(filters.studentId) : ALL}
                onValueChange={(value) =>
                    onChange({
                        ...filters,
                        studentId: value === ALL ? undefined : Number(value),
                    })
                }
            >
                <SelectTrigger className="w-full sm:w-48">
                    <SelectValue placeholder={t("studentPlaceholder")} />
                </SelectTrigger>
                <SelectContent>
                    <SelectItem value={ALL}>{t("allStudents")}</SelectItem>
                    {students.map((student) => (
                        <SelectItem key={student.id} value={String(student.id)}>
                            {student.firstName} {student.lastName}
                        </SelectItem>
                    ))}
                </SelectContent>
            </Select>
            <Select
                value={filters.status ?? ALL}
                onValueChange={(value) =>
                    onChange({
                        ...filters,
                        status:
                            value === ALL
                                ? undefined
                                : (value as EnrollmentStatusEnumKey),
                    })
                }
            >
                <SelectTrigger className="w-full sm:w-40">
                    <SelectValue placeholder={t("statusPlaceholder")} />
                </SelectTrigger>
                <SelectContent>
                    <SelectItem value={ALL}>{t("allStatuses")}</SelectItem>
                    {STATUSES.map((status) => (
                        <SelectItem key={status} value={status}>
                            {t(`statusOptions.${status}`)}
                        </SelectItem>
                    ))}
                </SelectContent>
            </Select>
        </div>
    );
}
