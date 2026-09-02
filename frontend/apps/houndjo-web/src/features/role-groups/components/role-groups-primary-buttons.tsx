import { ShieldPlus } from "lucide-react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { useRoleGroups } from "./role-groups-provider";

type RoleGroupsPrimaryButtonsProps = {
    canManageRoleGroups: boolean;
};

export function RoleGroupsPrimaryButtons({
    canManageRoleGroups,
}: RoleGroupsPrimaryButtonsProps) {
    const t = useTranslations("RoleGroups");
    const { setOpen } = useRoleGroups();

    if (!canManageRoleGroups) {
        return null;
    }

    return (
        <div className="flex gap-2">
            <Button className="space-x-1" onClick={() => setOpen("add")}>
                <span>{t("addRoleGroup")}</span> <ShieldPlus size={18} />
            </Button>
        </div>
    );
}
