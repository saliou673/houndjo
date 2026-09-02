"use client";

import { useEffect, useMemo, useState } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useConfirmEmailChange, useRequestEmailChange } from "@api-client";
import { CheckCircle2Icon } from "lucide-react";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import {
    Form,
    FormControl,
    FormField,
    FormItem,
    FormLabel,
    FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
    InputOTP,
    InputOTPGroup,
    InputOTPSlot,
} from "@/components/ui/input-otp";

function createRequestSchema(t: ReturnType<typeof useTranslations>) {
    return z.object({
        newEmail: z.email(t("newEmailInvalid")),
    });
}

function createConfirmSchema(t: ReturnType<typeof useTranslations>) {
    return z.object({
        code: z.string().length(4, t("codeLength")),
    });
}

type RequestFormValues = z.infer<ReturnType<typeof createRequestSchema>>;
type ConfirmFormValues = z.infer<ReturnType<typeof createConfirmSchema>>;

export function EmailChangeSection() {
    const t = useTranslations("SettingsAccount.emailChange");
    const tValidation = useTranslations(
        "SettingsAccount.emailChange.validation"
    );
    const [otpOpen, setOtpOpen] = useState(false);
    const [pendingEmail, setPendingEmail] = useState("");
    const [confirmed, setConfirmed] = useState(false);
    const [countdown, setCountdown] = useState(5);

    useEffect(() => {
        if (!confirmed) return;
        if (countdown <= 0) {
            window.location.href = "/sign-in";
            return;
        }
        const timer = setTimeout(() => setCountdown((c) => c - 1), 1000);
        return () => clearTimeout(timer);
    }, [confirmed, countdown]);

    const requestSchema = useMemo(
        () => createRequestSchema(tValidation),
        [tValidation]
    );
    const confirmSchema = useMemo(
        () => createConfirmSchema(tValidation),
        [tValidation]
    );

    const requestForm = useForm<RequestFormValues>({
        resolver: zodResolver(requestSchema),
        defaultValues: { newEmail: "" },
    });

    const confirmForm = useForm<ConfirmFormValues>({
        resolver: zodResolver(confirmSchema),
        defaultValues: { code: "" },
    });

    const requestMutation = useRequestEmailChange({
        mutation: {
            onSuccess: () => {
                setOtpOpen(true);
                toast.success(
                    t("codeSentToast", {
                        email: requestForm.getValues("newEmail"),
                    })
                );
            },
            onError: handleServerError,
        },
    });

    const confirmMutation = useConfirmEmailChange({
        mutation: {
            onSuccess: () => {
                setConfirmed(true);
            },
            onError: (error) => {
                handleServerError(error);
                confirmForm.reset();
            },
        },
    });

    function onRequestSubmit(values: RequestFormValues) {
        setPendingEmail(values.newEmail);
        requestMutation.mutate({ data: { newEmail: values.newEmail } });
    }

    function onConfirmSubmit(values: ConfirmFormValues) {
        confirmMutation.mutate({ data: { code: values.code } });
    }

    function handleCancel() {
        setOtpOpen(false);
        confirmForm.reset();
        requestForm.reset();
    }

    return (
        <>
            <div className="space-y-4">
                <div>
                    <h4 className="text-sm font-medium">{t("title")}</h4>
                    <p className="mt-1 text-sm text-muted-foreground">
                        {t("description")}
                    </p>
                </div>
                <Form {...requestForm}>
                    <form
                        onSubmit={requestForm.handleSubmit(onRequestSubmit)}
                        className="space-y-4"
                    >
                        <FormField
                            control={requestForm.control}
                            name="newEmail"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>
                                        {t("newEmailLabel")}
                                    </FormLabel>
                                    <FormControl>
                                        <Input
                                            type="email"
                                            placeholder={t(
                                                "newEmailPlaceholder"
                                            )}
                                            {...field}
                                        />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <Button
                            type="submit"
                            size="sm"
                            disabled={requestMutation.isPending}
                        >
                            {requestMutation.isPending
                                ? t("sendingButton")
                                : t("sendButton")}
                        </Button>
                    </form>
                </Form>
            </div>

            <Dialog
                open={otpOpen}
                onOpenChange={(open) => {
                    // Prevent closing by clicking outside or pressing Escape
                    if (!open) return;
                }}
            >
                <DialogContent showCloseButton={false}>
                    {confirmed ? (
                        <>
                            <DialogHeader>
                                <DialogTitle className="flex items-center gap-2">
                                    <CheckCircle2Icon className="size-5 text-green-500" />
                                    {t("changedTitle")}
                                </DialogTitle>
                                <DialogDescription>
                                    {t.rich("changedDescription", {
                                        email: pendingEmail,
                                        countdown,
                                        bold: (chunks) => (
                                            <span className="font-medium text-foreground">
                                                {chunks}
                                            </span>
                                        ),
                                    })}
                                </DialogDescription>
                            </DialogHeader>
                            <DialogFooter>
                                <Button
                                    onClick={() => {
                                        window.location.href = "/sign-in";
                                    }}
                                >
                                    {t("signInNowButton")}
                                </Button>
                            </DialogFooter>
                        </>
                    ) : (
                        <>
                            <DialogHeader>
                                <DialogTitle>
                                    {t("confirmTitle")}
                                </DialogTitle>
                                <DialogDescription>
                                    {t.rich("confirmDescription", {
                                        email: pendingEmail,
                                        bold: (chunks) => (
                                            <span className="font-medium text-foreground">
                                                {chunks}
                                            </span>
                                        ),
                                    })}
                                </DialogDescription>
                            </DialogHeader>
                            <Form {...confirmForm}>
                                <form
                                    onSubmit={confirmForm.handleSubmit(
                                        onConfirmSubmit
                                    )}
                                    className="space-y-6"
                                >
                                    <FormField
                                        control={confirmForm.control}
                                        name="code"
                                        render={({ field }) => (
                                            <FormItem className="flex flex-col items-center">
                                                <FormLabel className="sr-only">
                                                    {t("codeLabel")}
                                                </FormLabel>
                                                <FormControl>
                                                    <InputOTP
                                                        maxLength={4}
                                                        autoFocus
                                                        {...field}
                                                    >
                                                        <InputOTPGroup>
                                                            <InputOTPSlot
                                                                index={0}
                                                            />
                                                            <InputOTPSlot
                                                                index={1}
                                                            />
                                                            <InputOTPSlot
                                                                index={2}
                                                            />
                                                            <InputOTPSlot
                                                                index={3}
                                                            />
                                                        </InputOTPGroup>
                                                    </InputOTP>
                                                </FormControl>
                                                <FormMessage />
                                            </FormItem>
                                        )}
                                    />
                                    <DialogFooter>
                                        <Button
                                            type="button"
                                            variant="outline"
                                            onClick={handleCancel}
                                            disabled={confirmMutation.isPending}
                                        >
                                            {t("cancelButton")}
                                        </Button>
                                        <Button
                                            type="submit"
                                            disabled={confirmMutation.isPending}
                                        >
                                            {confirmMutation.isPending
                                                ? t("confirmingButton")
                                                : t("confirmButton")}
                                        </Button>
                                    </DialogFooter>
                                </form>
                            </Form>
                        </>
                    )}
                </DialogContent>
            </Dialog>
        </>
    );
}
