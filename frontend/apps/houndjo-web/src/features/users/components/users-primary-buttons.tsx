import { UserPlus } from "lucide-react";
import { useTranslations } from "next-intl";
import { Button } from "@/components/ui/button";
import { useUsers } from "./users-provider";

type UsersPrimaryButtonsProps = {
    canCreateUsers: boolean;
};

export function UsersPrimaryButtons({
    canCreateUsers,
}: UsersPrimaryButtonsProps) {
    const t = useTranslations("Users");
    const { setOpen } = useUsers();

    if (!canCreateUsers) {
        return null;
    }

    return (
        <div className="flex gap-2">
            <Button className="space-x-1" onClick={() => setOpen("add")}>
                <span>{t("addUser")}</span> <UserPlus size={18} />
            </Button>
        </div>
    );
}
