"use client";

import { useState } from "react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { ContentSection } from "../components/content-section";
import { AccountForm } from "./account-form";
import { ChangePasswordSection } from "./change-password-section";
import { DeleteAccountDialog } from "./delete-account-dialog";
import { EmailChangeSection } from "./email-change-section";
import { TwoFactorSection } from "./two-factor-section";

export function SettingsAccount() {
    const t = useTranslations("SettingsAccount");
    const [deleteOpen, setDeleteOpen] = useState(false);

    return (
        <ContentSection
            title={t("sectionTitle")}
            desc={t("sectionDescription")}
        >
            <div className="space-y-10">
                <AccountForm />
                <div className="space-y-4">
                    <Separator />
                    <ChangePasswordSection />
                </div>
                <div className="space-y-4">
                    <Separator />
                    <TwoFactorSection />
                </div>
                <div className="space-y-4">
                    <Separator />
                    <EmailChangeSection />
                </div>
                <div className="space-y-4">
                    <Separator />
                    <div>
                        <h4 className="text-sm font-medium text-destructive">
                            {t("dangerZoneTitle")}
                        </h4>
                        <p className="mt-1 text-sm text-muted-foreground">
                            {t("dangerZoneDescription")}
                        </p>
                    </div>
                    <Button
                        variant="destructive"
                        size="sm"
                        onClick={() => setDeleteOpen(true)}
                    >
                        {t("deleteAccountButton")}
                    </Button>
                </div>
                <DeleteAccountDialog
                    open={deleteOpen}
                    onOpenChange={setDeleteOpen}
                />
            </div>
        </ContentSection>
    );
}
