import { UsersActionDialog } from "./users-action-dialog";
import { UsersDeleteDialog } from "./users-delete-dialog";
import { useUsers } from "./users-provider";

interface UsersDialogsProps {
    canCreateUsers: boolean;
    canUpdateUsers: boolean;
    canDeleteUsers: boolean;
}

export function UsersDialogs({
    canCreateUsers,
    canUpdateUsers,
    canDeleteUsers,
}: UsersDialogsProps) {
    const { open, setOpen, currentRow, setCurrentRow } = useUsers();
    return (
        <>
            {canCreateUsers && (
                <UsersActionDialog
                    key="user-add"
                    open={open === "add"}
                    onOpenChange={() => setOpen("add")}
                />
            )}

            {currentRow && canUpdateUsers && (
                <>
                    <UsersActionDialog
                        key={`user-edit-${currentRow.id}`}
                        open={open === "edit"}
                        onOpenChange={() => {
                            setOpen("edit");
                            setTimeout(() => {
                                setCurrentRow(null);
                            }, 500);
                        }}
                        currentRow={currentRow}
                    />
                </>
            )}

            {currentRow && canDeleteUsers && (
                <UsersDeleteDialog
                    key={`user-delete-${currentRow.id}`}
                    open={open === "delete"}
                    onOpenChange={() => {
                        setOpen("delete");
                        setTimeout(() => {
                            setCurrentRow(null);
                        }, 500);
                    }}
                    currentRow={currentRow}
                />
            )}
        </>
    );
}
