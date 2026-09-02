"use client";

import { useEffect } from "react";
import { z } from "zod";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import {
    useGetSecuritySettingsAsAdmin,
    useUpsertSecuritySettingsAsAdmin,
    getSecuritySettingsAsAdminQueryKey,
} from "@api-client";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import {
    Form,
    FormControl,
    FormDescription,
    FormField,
    FormItem,
    FormLabel,
} from "@/components/ui/form";
import { Switch } from "@/components/ui/switch";

const formSchema = z.object({
    twoFactorRequired: z.boolean(),
});

type SecuritySettingsForm = z.infer<typeof formSchema>;

export function SecuritySettingsFeature() {
    const queryClient = useQueryClient();
    const { data: settings, isLoading } = useGetSecuritySettingsAsAdmin();

    const form = useForm<SecuritySettingsForm>({
        resolver: zodResolver(formSchema),
        defaultValues: {
            twoFactorRequired: false,
        },
    });

    useEffect(() => {
        if (settings) {
            form.reset({
                twoFactorRequired: settings.twoFactorRequired ?? false,
            });
        }
    }, [settings, form]);

    const upsertMutation = useUpsertSecuritySettingsAsAdmin({
        mutation: {
            onSuccess: async () => {
                await queryClient.invalidateQueries({
                    queryKey: getSecuritySettingsAsAdminQueryKey(),
                });
                toast.success("Security settings saved.");
            },
            onError: handleServerError,
        },
    });

    const isPending = upsertMutation.isPending;

    const onSubmit = (values: SecuritySettingsForm) => {
        upsertMutation.mutate({
            data: {
                twoFactorRequired: values.twoFactorRequired,
            },
        });
    };

    return (
        <div className="w-full max-w-2xl">
            <div className="mb-6">
                <h2 className="text-2xl font-bold tracking-tight">
                    Security Settings
                </h2>
                <p className="text-muted-foreground">
                    Configure application-wide security policies.
                </p>
            </div>

            {isLoading ? (
                <p className="text-muted-foreground">
                    Loading security settings...
                </p>
            ) : (
                <Form {...form}>
                    <form
                        onSubmit={form.handleSubmit(onSubmit)}
                        className="space-y-8"
                    >
                        <div className="space-y-4">
                            <h3 className="font-semibold">
                                Two-Factor Authentication
                            </h3>
                            <FormField
                                control={form.control}
                                name="twoFactorRequired"
                                render={({ field }) => (
                                    <FormItem className="flex flex-row items-center justify-between rounded-lg border p-4">
                                        <div className="space-y-0.5">
                                            <FormLabel className="text-base">
                                                Require 2FA for all users
                                            </FormLabel>
                                            <FormDescription>
                                                When enabled, all users must
                                                configure two-factor
                                                authentication before they can
                                                log in.
                                            </FormDescription>
                                        </div>
                                        <FormControl>
                                            <Switch
                                                checked={field.value}
                                                onCheckedChange={field.onChange}
                                                disabled={isPending}
                                            />
                                        </FormControl>
                                    </FormItem>
                                )}
                            />
                        </div>

                        <div className="flex justify-end">
                            <Button type="submit" disabled={isPending}>
                                {isPending ? "Saving…" : "Save settings"}
                            </Button>
                        </div>
                    </form>
                </Form>
            )}
        </div>
    );
}
