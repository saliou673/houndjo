"use client";

import { useState, type FormEvent } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { type AppConfiguration, useDelete } from "@api-client";
import { AlertTriangle } from "lucide-react";
import { handleServerError } from "@/lib/handle-server-error";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { ConfirmDialog } from "@/components/confirm-dialog";

type ConfigurationsDeleteDialogProps = {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    currentRow: AppConfiguration | null;
    onDeleted: () => void;
};

export function ConfigurationsDeleteDialog({
    open,
    onOpenChange,
    currentRow,
    onDeleted,
}: ConfigurationsDeleteDialogProps) {
    const [value, setValue] = useState("");
    const queryClient = useQueryClient();
    const { mutate: deleteConfiguration, isPending: isDeleting } = useDelete({
        mutation: {
            onSuccess: async () => {
                // getAppConfigurationsAsAdminQueryKey() requires a filter/pageable
                // params argument, so invalidate by the shared URL prefix instead
                // of reconstructing whatever params the list view happened to use.
                await queryClient.invalidateQueries({
                    queryKey: [{ url: "/api/admin/configurations" }],
                });

                setValue("");
                onOpenChange(false);
                onDeleted();
            },
            onError: handleServerError,
        },
    });

    const expectedCode = currentRow?.code?.trim() ?? "";
    const isConfirmed = value.trim() === expectedCode;

    const handleDelete = () => {
        if (!currentRow?.id || !isConfirmed) {
            return;
        }

        deleteConfiguration({ id: currentRow.id });
    };

    const handleOpenChange = (nextOpen: boolean) => {
        if (!isDeleting) {
            onOpenChange(nextOpen);
            if (!nextOpen) {
                setValue("");
            }
        }
    };

    const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
        event.preventDefault();
        handleDelete();
    };
    return (
        <ConfirmDialog
            open={open}
            onOpenChange={handleOpenChange}
            handleConfirm={handleDelete}
            confirmForm="configuration-delete-form"
            confirmButtonType="submit"
            disabled={!isConfirmed}
            isLoading={isDeleting}
            title={
                <span className="text-destructive">
                    <AlertTriangle
                        className="me-1 inline-block stroke-destructive"
                        size={18}
                    />{" "}
                    Delete Configuration
                </span>
            }
            desc={
                <form
                    id="configuration-delete-form"
                    className="space-y-4"
                    onSubmit={handleSubmit}
                >
                    <p className="mb-2">
                        Delete configuration{" "}
                        <span className="font-bold">{currentRow?.code}</span> in{" "}
                        <span className="font-bold">
                            {currentRow?.category}
                        </span>
                        ?
                        <br />
                        This action permanently removes the configuration entry
                        from the system.
                    </p>

                    <Label className="my-2">
                        Enter configuration code to confirm:
                        <Input
                            value={value}
                            onChange={(event) => setValue(event.target.value)}
                            placeholder="Enter code"
                        />
                    </Label>

                    <Alert variant="destructive">
                        <AlertTitle>Warning!</AlertTitle>
                        <AlertDescription>
                            Backend rules still apply. If this configuration is
                            referenced elsewhere, deletion can fail.
                        </AlertDescription>
                    </Alert>
                </form>
            }
            confirmText="Delete"
            destructive
        />
    );
}
