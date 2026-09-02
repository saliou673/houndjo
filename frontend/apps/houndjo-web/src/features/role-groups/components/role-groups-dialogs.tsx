import { RoleGroupsActionDialog } from "./role-groups-action-dialog";
import { RoleGroupsDeleteDialog } from "./role-groups-delete-dialog";
import { useRoleGroups } from "./role-groups-provider";

interface RoleGroupsDialogsProps {
    canManageRoleGroups: boolean;
}

export function RoleGroupsDialogs({
    canManageRoleGroups,
}: RoleGroupsDialogsProps) {
    const { open, setOpen, currentRow, setCurrentRow } = useRoleGroups();

    return (
        <>
            {canManageRoleGroups && (
                <RoleGroupsActionDialog
                    key="role-group-add"
                    open={open === "add"}
                    onOpenChange={() => setOpen("add")}
                />
            )}

            {currentRow && canManageRoleGroups && (
                <>
                    <RoleGroupsActionDialog
                        key={`role-group-edit-${currentRow.id}`}
                        open={open === "edit"}
                        onOpenChange={() => {
                            setOpen("edit");
                            setTimeout(() => {
                                setCurrentRow(null);
                            }, 500);
                        }}
                        currentRow={currentRow}
                    />
                    <RoleGroupsDeleteDialog
                        key={`role-group-delete-${currentRow.id}`}
                        open={open === "delete"}
                        onOpenChange={() => {
                            setOpen("delete");
                            setTimeout(() => {
                                setCurrentRow(null);
                            }, 500);
                        }}
                        currentRow={currentRow}
                    />
                </>
            )}
        </>
    );
}
