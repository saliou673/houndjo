import { useTranslations } from "next-intl";
import { ContentSection } from "../components/content-section";
import { ProfileForm } from "./profile-form";

export function SettingsProfile() {
    const t = useTranslations("SettingsProfile");

    return (
        <ContentSection
            title={t("sectionTitle")}
            desc={t("sectionDescription")}
        >
            <ProfileForm />
        </ContentSection>
    );
}
