import { useTranslations } from "next-intl";
import { ContentSection } from "../components/content-section";
import { NotificationsForm } from "./notifications-form";

export function SettingsNotifications() {
    const t = useTranslations("SettingsNotifications");

    return (
        <ContentSection
            title={t("sectionTitle")}
            desc={t("sectionDescription")}
        >
            <NotificationsForm />
        </ContentSection>
    );
}
