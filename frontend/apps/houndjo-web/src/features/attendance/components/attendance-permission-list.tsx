"use client";
import { useQueryClient } from "@tanstack/react-query";
import {
    useGetAttendancePermissionsByStudent,
    useUpdateAttendancePermissionStatus,
    getAttendancePermissionsByStudentQueryKey,
} from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { hasValidId } from "../data/policy";

export function AttendancePermissionList({
    studentId,
    canUpdatePermission,
}: {
    studentId: number;
    canUpdatePermission: boolean;
}) {
    const t = useTranslations("Attendance.studentDialog");
    const tPermissionStatus = useTranslations(
        "Attendance.permissionStatusOptions"
    );
    const queryClient = useQueryClient();
    const {
        data,
        isLoading: isPermissionsLoading,
        isError,
    } = useGetAttendancePermissionsByStudent({ studentId });
    const permissions = (data ?? []).filter(hasValidId);
    const isPermissionsError =
        isError ||
        new Set(permissions.map((entry) => entry.id)).size !==
            permissions.length ||
        permissions.length !== (data ?? []).length;
    const invalidatePermissions = () =>
        queryClient.invalidateQueries({
            queryKey: getAttendancePermissionsByStudentQueryKey({ studentId }),
        });
    const { mutate: updateStatus, isPending: isUpdatingStatus } =
        useUpdateAttendancePermissionStatus({
            mutation: {
                onSuccess: async () => {
                    await invalidatePermissions();
                    toast.success(t("permissions.statusUpdatedToast"));
                },
                onError: handleServerError,
            },
        });

    return (
        <>
            {isPermissionsError && (
                <p className="text-sm text-destructive">{t("errorFallback")}</p>
            )}{" "}
            {isPermissionsLoading && (
                <p className="text-sm text-muted-foreground">{t("loading")}</p>
            )}
            {!isPermissionsLoading &&
                !isPermissionsError &&
                permissions.length === 0 && (
                    <p className="text-sm text-muted-foreground">
                        {t("permissions.noResults")}
                    </p>
                )}
            {!isPermissionsLoading &&
                !isPermissionsError &&
                permissions.length > 0 && (
                    <div className="flex flex-col gap-2">
                        {permissions.map((permission) => (
                            <Card key={permission.id}>
                                <CardContent className="flex flex-wrap items-center justify-between gap-2 pt-4">
                                    <div>
                                        <p className="text-sm font-medium">
                                            {permission.fromDate} —{" "}
                                            {permission.toDate}
                                        </p>
                                        {permission.reason && (
                                            <p className="text-sm text-muted-foreground">
                                                {permission.reason}
                                            </p>
                                        )}
                                    </div>
                                    <div className="flex items-center gap-2">
                                        <Badge
                                            variant={
                                                permission.status === "APPROVED"
                                                    ? "default"
                                                    : permission.status ===
                                                        "REJECTED"
                                                      ? "destructive"
                                                      : "secondary"
                                            }
                                        >
                                            {permission.status
                                                ? tPermissionStatus(
                                                      permission.status
                                                  )
                                                : "—"}
                                        </Badge>
                                        {permission.status === "PENDING" &&
                                            canUpdatePermission && (
                                                <>
                                                    <Button
                                                        type="button"
                                                        size="sm"
                                                        variant="outline"
                                                        disabled={
                                                            isUpdatingStatus
                                                        }
                                                        onClick={() =>
                                                            updateStatus({
                                                                id: permission.id,
                                                                data: {
                                                                    status: "APPROVED",
                                                                },
                                                            })
                                                        }
                                                    >
                                                        {t(
                                                            "permissions.approveAction"
                                                        )}
                                                    </Button>
                                                    <Button
                                                        type="button"
                                                        size="sm"
                                                        variant="outline"
                                                        disabled={
                                                            isUpdatingStatus
                                                        }
                                                        onClick={() =>
                                                            updateStatus({
                                                                id: permission.id,
                                                                data: {
                                                                    status: "REJECTED",
                                                                },
                                                            })
                                                        }
                                                    >
                                                        {t(
                                                            "permissions.rejectAction"
                                                        )}
                                                    </Button>
                                                </>
                                            )}
                                    </div>
                                </CardContent>
                            </Card>
                        ))}
                    </div>
                )}
        </>
    );
}
