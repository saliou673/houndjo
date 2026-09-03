"use client";

import { useEffect, useState } from "react";
import { axiosInstance } from "@api-client";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Main } from "@/components/layout/main";
import { useActiveOrgStore } from "@/stores/active-org-store";

type Member = { id: number; userEmail: string; userFullName: string; role: string; status: string };
type Invitation = { id: number; email: string; role: string; status: string };

export function Members() {
    const t = useTranslations("Members");
    const organization = useActiveOrgStore((state) => state.activeOrganization);
    const [members, setMembers] = useState<Member[]>([]);
    const [invitations, setInvitations] = useState<Invitation[]>([]);
    const [email, setEmail] = useState("");
    const [role, setRole] = useState("TEACHER");
    const load = async () => { if (!organization) return; const [memberResponse, invitationResponse] = await Promise.all([axiosInstance.get(`/api/organizations/${organization.id}/memberships`), axiosInstance.get(`/api/organizations/${organization.id}/invitations`)]); setMembers(memberResponse.data.items ?? []); setInvitations(invitationResponse.data.items ?? []); };
    useEffect(() => { void load(); }, [organization]);
    async function invite(event: React.FormEvent) { event.preventDefault(); if (!organization) return; await axiosInstance.post(`/api/organizations/${organization.id}/invitations`, { email, role }); setEmail(""); await load(); }
    async function changeRole(member: Member, nextRole: string) { if (!organization) return; await axiosInstance.patch(`/api/organizations/${organization.id}/memberships/${member.id}/role`, { role: nextRole }); await load(); }
    async function revoke(member: Member) { if (!organization || !window.confirm(t("confirmRevoke"))) return; await axiosInstance.delete(`/api/organizations/${organization.id}/memberships/${member.id}`); await load(); }
    if (!organization) return <Main><Card><CardHeader><CardTitle>{t("noOrganization")}</CardTitle><CardDescription>{t("selectOrganization")}</CardDescription></CardHeader></Card></Main>;
    return <Main className="space-y-6"><div><h1 className="text-2xl font-bold tracking-tight">{t("title")}</h1><p className="text-muted-foreground">{t("description")}</p></div><Card><CardHeader><CardTitle>{t("inviteTitle")}</CardTitle></CardHeader><CardContent><form onSubmit={invite} className="flex flex-col gap-3 sm:flex-row sm:items-end"><div className="flex-1 space-y-2"><Label htmlFor="member-email">{t("email")}</Label><Input id="member-email" type="email" required value={email} onChange={(event) => setEmail(event.target.value)} /></div><div className="space-y-2"><Label htmlFor="member-role">{t("role")}</Label><select id="member-role" className="h-9 rounded-md border bg-background px-3 text-sm" value={role} onChange={(event) => setRole(event.target.value)}><option value="SCHOOL_ADMIN">{t("roles.SCHOOL_ADMIN")}</option><option value="TEACHER">{t("roles.TEACHER")}</option></select></div><Button type="submit">{t("invite")}</Button></form></CardContent></Card><Card><CardHeader><CardTitle>{t("membersTitle")}</CardTitle></CardHeader><CardContent><div className="grid gap-3">{members.map((member) => <div key={member.id} className="flex flex-col gap-3 rounded-md border p-3 sm:flex-row sm:items-center sm:justify-between"><div><p className="font-medium">{member.userFullName}</p><p className="text-sm text-muted-foreground">{member.userEmail}</p></div><div className="flex items-center gap-2"><select aria-label={t("role")} className="h-9 rounded-md border bg-background px-2 text-sm" value={member.role} onChange={(event) => void changeRole(member, event.target.value)}><option value="SCHOOL_OWNER">{t("roles.SCHOOL_OWNER")}</option><option value="SCHOOL_ADMIN">{t("roles.SCHOOL_ADMIN")}</option><option value="TEACHER">{t("roles.TEACHER")}</option></select><Button variant="destructive" size="sm" onClick={() => void revoke(member)}>{t("revoke")}</Button></div></div>)}{members.length === 0 && <p className="text-sm text-muted-foreground">{t("empty")}</p>}</div></CardContent></Card><Card><CardHeader><CardTitle>{t("pendingTitle")}</CardTitle></CardHeader><CardContent className="space-y-2">{invitations.map((invitation) => <div key={invitation.id} className="flex justify-between rounded-md border p-3 text-sm"><span>{invitation.email}</span><span className="text-muted-foreground">{invitation.role}</span></div>)}{invitations.length === 0 && <p className="text-sm text-muted-foreground">{t("empty")}</p>}</CardContent></Card></Main>;
}
