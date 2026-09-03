import { create } from "zustand";
import { persist } from "zustand/middleware";

export type ActiveOrganization = { id: number; name: string; slug: string };
type State = { activeOrganization: ActiveOrganization | null; setActiveOrganization: (organization: ActiveOrganization) => void };
export const useActiveOrgStore = create<State>()(persist((set) => ({
    activeOrganization: null,
    setActiveOrganization: (activeOrganization) => set({ activeOrganization }),
}), { name: "houndjo-active-organization" }));
