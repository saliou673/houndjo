import React, { useState } from "react";
import useDialogState from "@/hooks/use-dialog-state";
import { type RoleGroupRow } from "../data/schema";

type RoleGroupsDialogType = "add" | "edit" | "delete";

type RoleGroupsContextType = {
    open: RoleGroupsDialogType | null;
    setOpen: (str: RoleGroupsDialogType | null) => void;
    currentRow: RoleGroupRow | null;
    setCurrentRow: React.Dispatch<React.SetStateAction<RoleGroupRow | null>>;
};

const RoleGroupsContext = React.createContext<RoleGroupsContextType | null>(
    null
);

export function RoleGroupsProvider({
    children,
}: {
    children: React.ReactNode;
}) {
    const [open, setOpen] = useDialogState<RoleGroupsDialogType>(null);
    const [currentRow, setCurrentRow] = useState<RoleGroupRow | null>(null);

    return (
        <RoleGroupsContext value={{ open, setOpen, currentRow, setCurrentRow }}>
            {children}
        </RoleGroupsContext>
    );
}

export const useRoleGroups = () => {
    const context = React.useContext(RoleGroupsContext);

    if (!context) {
        throw new Error(
            "useRoleGroups has to be used within <RoleGroupsContext>"
        );
    }

    return context;
};
