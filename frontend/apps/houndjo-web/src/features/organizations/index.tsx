"use client";

import { useEffect, useState } from "react";
import { axiosInstance } from "@api-client";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Main } from "@/components/layout/main";
import { useActiveOrgStore, type ActiveOrganization } from "@/stores/active-org-store";

type Organization = ActiveOrganization & { contactEmail: string; phoneNumber?: string; address?: string; defaultCurrencyCode: string; defaultLanguageKey: string; timezone: string };
const api = axiosInstance;

export function Organizations() {
    const t = useTranslations("Organizations");
    const { activeOrganization, setActiveOrganization } = useActiveOrgStore();
    const [organizations, setOrganizations] = useState<Organization[]>([]);
    const [form, setForm] = useState({ name: "", contactEmail: "", phoneNumber: "", address: "", defaultCurrencyCode: "GNF", defaultLanguageKey: "fr" });
    const [loading, setLoading] = useState(false);

    useEffect(() => { void api.get<Organization[]>("/api/organizations/mine").then(({ data }) => { setOrganizations(data); if (!activeOrganization && data[0]) setActiveOrganization(data[0]); }); }, [activeOrganization, setActiveOrganization]);
    useEffect(() => { if (activeOrganization) { const organization = organizations.find((item) => item.id === activeOrganization.id); if (organization) setForm({ name: organization.name, contactEmail: organization.contactEmail, phoneNumber: organization.phoneNumber ?? "", address: organization.address ?? "", defaultCurrencyCode: organization.defaultCurrencyCode, defaultLanguageKey: organization.defaultLanguageKey }); } }, [activeOrganization, organizations]);

    async function save(event: React.FormEvent) { event.preventDefault(); setLoading(true); try { const { data } = activeOrganization ? await api.put<Organization>(`/api/organizations/${activeOrganization.id}`, form) : await api.post<Organization>("/api/organizations/register", form); setOrganizations((items) => activeOrganization ? items.map((item) => item.id === data.id ? data : item) : [...items, data]); setActiveOrganization(data); } finally { setLoading(false); } }
    return <Main className="space-y-6"><div><h1 className="text-2xl font-bold tracking-tight">{t("title")}</h1><p className="text-muted-foreground">{t("description")}</p></div><Card><CardHeader><CardTitle>{t("switcherTitle")}</CardTitle><CardDescription>{t("switcherDescription")}</CardDescription></CardHeader><CardContent className="flex flex-wrap gap-2">{organizations.map((organization) => <Button key={organization.id} variant={activeOrganization?.id === organization.id ? "default" : "outline"} onClick={() => setActiveOrganization(organization)}>{organization.name}</Button>)}</CardContent></Card><Card><CardHeader><CardTitle>{activeOrganization ? t("editTitle") : t("createTitle")}</CardTitle></CardHeader><CardContent><form onSubmit={save} className="grid gap-4 sm:grid-cols-2">{(["name", "contactEmail", "phoneNumber", "address", "defaultCurrencyCode", "defaultLanguageKey"] as const).map((field) => <div key={field} className="space-y-2"><Label htmlFor={field}>{t(`fields.${field}`)}</Label><Input id={field} required={field === "name" || field === "contactEmail"} type={field === "contactEmail" ? "email" : "text"} value={form[field]} onChange={(event) => setForm({ ...form, [field]: event.target.value })} /></div>)}<div className="sm:col-span-2"><Button type="submit" disabled={loading}>{loading ? t("saving") : t("save")}</Button></div></form></CardContent></Card></Main>;
}
