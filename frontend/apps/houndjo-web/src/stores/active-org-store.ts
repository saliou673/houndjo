import { create } from "zustand";
import { persist } from "zustand/middleware";

export type ActiveOrganization = { id: number; name: string; slug: string };
type State = {
    activeOrganization: ActiveOrganization | null;
    hasHydrated: boolean;
    setActiveOrganization: (organization: ActiveOrganization) => void;
    setHasHydrated: (hasHydrated: boolean) => void;
};
export const useActiveOrgStore = create<State>()(
    persist(
        (set) => ({
            activeOrganization: null,
            hasHydrated: false,
            setActiveOrganization: (activeOrganization) => set({ activeOrganization }),
            setHasHydrated: (hasHydrated) => set({ hasHydrated }),
        }),
        {
            name: "houndjo-active-organization",
            onRehydrateStorage: () => (state) => state?.setHasHydrated(true),
        },
    ),
);
