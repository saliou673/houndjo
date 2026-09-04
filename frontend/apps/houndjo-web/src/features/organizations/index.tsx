"use client";

import { useEffect, useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import { useGetMyOrganizations, useRegisterSchool, useUpdateOrganization, type Organization } from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Main } from "@/components/layout/main";
import { useActiveOrgStore, type ActiveOrganization } from "@/stores/active-org-store";

const FIELDS = ["name", "contactEmail", "phoneNumber", "address", "defaultCurrencyCode", "defaultLanguageKey"] as const;
type FormField = (typeof FIELDS)[number];
type FormState = Record<FormField, string> & { timezone?: string };

function buildFormState(organization?: Organization): FormState {
    return {
        name: organization?.name ?? "",
        contactEmail: organization?.contactEmail ?? "",
        phoneNumber: organization?.phoneNumber ?? "",
        address: organization?.address ?? "",
        defaultCurrencyCode: organization?.defaultCurrencyCode ?? "GNF",
        defaultLanguageKey: organization?.defaultLanguageKey ?? "fr",
        timezone: organization?.timezone,
    };
}

function toActiveOrganization(organization: Organization): ActiveOrganization | undefined {
    if (organization.id === undefined || !organization.name || !organization.slug) return undefined;
    return { id: organization.id, name: organization.name, slug: organization.slug };
}

type OrganizationFormProps = {
    organization?: Organization;
    saving: boolean;
    onSubmit: (form: FormState) => void;
};

function OrganizationForm({ organization, saving, onSubmit }: OrganizationFormProps) {
    const t = useTranslations("Organizations");
    const [form, setForm] = useState<FormState>(() => buildFormState(organization));

    return (
        <form
            onSubmit={(event) => {
                event.preventDefault();
                onSubmit(form);
            }}
            className="grid gap-4 sm:grid-cols-2"
        >
            {FIELDS.map((field) => (
                <div key={field} className="space-y-2">
                    <Label htmlFor={field}>{t(`fields.${field}`)}</Label>
                    <Input
                        id={field}
                        required={field === "name" || field === "contactEmail"}
                        type={field === "contactEmail" ? "email" : "text"}
                        value={form[field]}
                        onChange={(event) => setForm({ ...form, [field]: event.target.value })}
                    />
                </div>
            ))}
            <div className="sm:col-span-2">
                <Button type="submit" disabled={saving}>
                    {saving ? t("saving") : t("save")}
                </Button>
            </div>
        </form>
    );
}

export function Organizations() {
    const t = useTranslations("Organizations");
    const queryClient = useQueryClient();
    const { activeOrganization, setActiveOrganization, hasHydrated } = useActiveOrgStore();

    const { data: organizations = [] } = useGetMyOrganizations(undefined, { query: { enabled: hasHydrated } });

    useEffect(() => {
        if (!hasHydrated || activeOrganization || organizations.length === 0) return;
        const active = toActiveOrganization(organizations[0]);
        if (active) setActiveOrganization(active);
    }, [hasHydrated, activeOrganization, organizations, setActiveOrganization]);

    async function invalidateOrganizations() {
        await queryClient.invalidateQueries({ queryKey: [{ url: "/api/organizations/mine" }] });
    }

    const { mutate: registerSchool, isPending: isCreating } = useRegisterSchool({
        mutation: {
            onSuccess: async (data) => {
                await invalidateOrganizations();
                const active = toActiveOrganization(data);
                if (active) setActiveOrganization(active);
                toast.success(t("savedToast"));
            },
            onError: handleServerError,
        },
    });

    const { mutate: updateOrganization, isPending: isUpdating } = useUpdateOrganization({
        mutation: {
            onSuccess: async (data) => {
                await invalidateOrganizations();
                const active = toActiveOrganization(data);
                if (active) setActiveOrganization(active);
                toast.success(t("savedToast"));
            },
            onError: handleServerError,
        },
    });

    const saving = isCreating || isUpdating;
    const selectedOrganization = organizations.find((item) => item.id === activeOrganization?.id);

    function save(form: FormState) {
        if (activeOrganization) {
            updateOrganization({ id: activeOrganization.id, data: form });
        } else {
            registerSchool({ data: form });
        }
    }

    return (
        <Main className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold tracking-tight">{t("title")}</h1>
                <p className="text-muted-foreground">{t("description")}</p>
            </div>
            <Card>
                <CardHeader>
                    <CardTitle>{t("switcherTitle")}</CardTitle>
                    <CardDescription>{t("switcherDescription")}</CardDescription>
                </CardHeader>
                <CardContent className="flex flex-wrap gap-2">
                    {organizations.map((organization) => (
                        <Button
                            key={organization.id}
                            variant={activeOrganization?.id === organization.id ? "default" : "outline"}
                            onClick={() => {
                                const active = toActiveOrganization(organization);
                                if (active) setActiveOrganization(active);
                            }}
                        >
                            {organization.name}
                        </Button>
                    ))}
                </CardContent>
            </Card>
            <Card>
                <CardHeader>
                    <CardTitle>{activeOrganization ? t("editTitle") : t("createTitle")}</CardTitle>
                </CardHeader>
                <CardContent>
                    <OrganizationForm
                        key={activeOrganization?.id ?? "new"}
                        organization={selectedOrganization}
                        saving={saving}
                        onSubmit={save}
                    />
                </CardContent>
            </Card>
        </Main>
    );
}
