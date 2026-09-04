"use client";

import { useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import {
    useGetMemberships,
    useChangeMembershipRole,
    useRevokeMembership,
    useInvite,
    useList,
    type Membership,
    type MembershipRoleEnumKey,
} from "@api-client";
import { useTranslations } from "next-intl";
import { toast } from "sonner";
import { handleServerError } from "@/lib/handle-server-error";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Main } from "@/components/layout/main";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { useActiveOrgStore } from "@/stores/active-org-store";

const PAGEABLE = { pageable: { page: 0, size: 100 } };
const INVITABLE_ROLES: MembershipRoleEnumKey[] = ["SCHOOL_ADMIN", "TEACHER"];
const ALL_ROLES: MembershipRoleEnumKey[] = ["SCHOOL_OWNER", "SCHOOL_ADMIN", "TEACHER"];

export function Members() {
    const t = useTranslations("Members");
    const queryClient = useQueryClient();
    const organization = useActiveOrgStore((state) => state.activeOrganization);
    const orgId = organization?.id ?? 0;

    const [email, setEmail] = useState("");
    const [role, setRole] = useState<MembershipRoleEnumKey>("TEACHER");
    const [revokeTarget, setRevokeTarget] = useState<Membership | null>(null);
    const [ownerPromotionTarget, setOwnerPromotionTarget] = useState<Membership | null>(null);

    const { data: membersResult } = useGetMemberships(orgId, PAGEABLE, undefined, {
        query: { enabled: !!organization },
    });
    const { data: invitationsResult } = useList(orgId, PAGEABLE, undefined, {
        query: { enabled: !!organization },
    });
    const members = membersResult?.items ?? [];
    const invitations = invitationsResult?.items ?? [];

    async function invalidateMembers() {
        await queryClient.invalidateQueries({
            queryKey: [{ url: "/api/organizations/:orgId/memberships", params: { orgId } }],
        });
    }

    async function invalidateInvitations() {
        await queryClient.invalidateQueries({
            queryKey: [{ url: "/api/organizations/:orgId/invitations", params: { orgId } }],
        });
    }

    const { mutate: invite, isPending: isInviting } = useInvite({
        mutation: {
            onSuccess: async () => {
                await invalidateInvitations();
                setEmail("");
                toast.success(t("inviteSentToast"));
            },
            onError: handleServerError,
        },
    });

    const { mutate: changeRole } = useChangeMembershipRole({
        mutation: {
            onSuccess: async () => {
                await invalidateMembers();
                toast.success(t("roleChangedToast"));
            },
            onError: handleServerError,
        },
    });

    const { mutate: revokeMembership, isPending: isRevoking } = useRevokeMembership({
        mutation: {
            onSuccess: async () => {
                await invalidateMembers();
                setRevokeTarget(null);
                toast.success(t("revokedToast"));
            },
            onError: handleServerError,
        },
    });

    function handleInvite(event: React.FormEvent) {
        event.preventDefault();
        invite({ orgId, data: { email, role } });
    }

    function requestRoleChange(member: Membership, nextRole: MembershipRoleEnumKey) {
        if (nextRole === "SCHOOL_OWNER") {
            setOwnerPromotionTarget(member);
            return;
        }
        if (member.id !== undefined) changeRole({ orgId, id: member.id, data: { role: nextRole } });
    }

    if (!organization) {
        return (
            <Main>
                <Card>
                    <CardHeader>
                        <CardTitle>{t("noOrganization")}</CardTitle>
                        <CardDescription>{t("selectOrganization")}</CardDescription>
                    </CardHeader>
                </Card>
            </Main>
        );
    }

    return (
        <Main className="space-y-6">
            <div>
                <h1 className="text-2xl font-bold tracking-tight">{t("title")}</h1>
                <p className="text-muted-foreground">{t("description")}</p>
            </div>
            <Card>
                <CardHeader>
                    <CardTitle>{t("inviteTitle")}</CardTitle>
                </CardHeader>
                <CardContent>
                    <form onSubmit={handleInvite} className="flex flex-col gap-3 sm:flex-row sm:items-end">
                        <div className="flex-1 space-y-2">
                            <Label htmlFor="member-email">{t("email")}</Label>
                            <Input
                                id="member-email"
                                type="email"
                                required
                                value={email}
                                onChange={(event) => setEmail(event.target.value)}
                            />
                        </div>
                        <div className="space-y-2">
                            <Label htmlFor="member-role">{t("role")}</Label>
                            <select
                                id="member-role"
                                className="h-9 rounded-md border bg-background px-3 text-sm"
                                value={role}
                                onChange={(event) => setRole(event.target.value as MembershipRoleEnumKey)}
                            >
                                {INVITABLE_ROLES.map((value) => (
                                    <option key={value} value={value}>
                                        {t(`roles.${value}`)}
                                    </option>
                                ))}
                            </select>
                        </div>
                        <Button type="submit" disabled={isInviting}>
                            {t("invite")}
                        </Button>
                    </form>
                </CardContent>
            </Card>
            <Card>
                <CardHeader>
                    <CardTitle>{t("membersTitle")}</CardTitle>
                </CardHeader>
                <CardContent>
                    {members.length === 0 ? (
                        <p className="text-sm text-muted-foreground">{t("empty")}</p>
                    ) : (
                        <Table>
                            <TableHeader>
                                <TableRow>
                                    <TableHead>{t("email")}</TableHead>
                                    <TableHead>{t("role")}</TableHead>
                                    <TableHead />
                                </TableRow>
                            </TableHeader>
                            <TableBody>
                                {members.map((member) => (
                                    <TableRow key={member.id}>
                                        <TableCell>
                                            <p className="font-medium">{member.userFullName}</p>
                                            <p className="text-sm text-muted-foreground">{member.userEmail}</p>
                                        </TableCell>
                                        <TableCell>
                                            <select
                                                aria-label={t("role")}
                                                className="h-9 rounded-md border bg-background px-2 text-sm"
                                                value={member.role}
                                                onChange={(event) =>
                                                    requestRoleChange(
                                                        member,
                                                        event.target.value as MembershipRoleEnumKey,
                                                    )
                                                }
                                            >
                                                {ALL_ROLES.map((value) => (
                                                    <option key={value} value={value}>
                                                        {t(`roles.${value}`)}
                                                    </option>
                                                ))}
                                            </select>
                                        </TableCell>
                                        <TableCell className="text-end">
                                            <Button
                                                variant="destructive"
                                                size="sm"
                                                onClick={() => setRevokeTarget(member)}
                                            >
                                                {t("revoke")}
                                            </Button>
                                        </TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    )}
                </CardContent>
            </Card>
            <Card>
                <CardHeader>
                    <CardTitle>{t("pendingTitle")}</CardTitle>
                </CardHeader>
                <CardContent className="space-y-2">
                    {invitations.map((invitation) => (
                        <div key={invitation.id} className="flex justify-between rounded-md border p-3 text-sm">
                            <span>{invitation.email}</span>
                            <span className="text-muted-foreground">
                                {invitation.role ? t(`roles.${invitation.role}`) : null}
                            </span>
                        </div>
                    ))}
                    {invitations.length === 0 && <p className="text-sm text-muted-foreground">{t("empty")}</p>}
                </CardContent>
            </Card>

            <ConfirmDialog
                open={revokeTarget !== null}
                onOpenChange={(open) => !open && setRevokeTarget(null)}
                title={t("revoke")}
                desc={t("confirmRevoke")}
                destructive
                isLoading={isRevoking}
                handleConfirm={() => {
                    if (revokeTarget?.id !== undefined) revokeMembership({ orgId, id: revokeTarget.id });
                }}
            />

            <ConfirmDialog
                open={ownerPromotionTarget !== null}
                onOpenChange={(open) => !open && setOwnerPromotionTarget(null)}
                title={t("roles.SCHOOL_OWNER")}
                desc={t("confirmOwnerPromotion")}
                handleConfirm={() => {
                    if (ownerPromotionTarget?.id !== undefined) {
                        changeRole({ orgId, id: ownerPromotionTarget.id, data: { role: "SCHOOL_OWNER" } });
                    }
                    setOwnerPromotionTarget(null);
                }}
            />
        </Main>
    );
}
