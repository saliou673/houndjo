import type { Metadata } from "next";
import { MaintenanceError } from "@/features/errors/maintenance-error";

export const metadata: Metadata = {
    title: "Maintenance",
};

export default function MaintenanceErrorPage() {
    return <MaintenanceError />;
}
