import { useTranslations } from "next-intl";
import { ContentSection } from "../components/content-section";
import { AppearanceForm } from "./appearance-form";

export function SettingsAppearance() {
    const t = useTranslations("SettingsAppearance");

    return (
        <ContentSection
            title={t("sectionTitle")}
            desc={t("sectionDescription")}
        >
            <AppearanceForm />
        </ContentSection>
    );
}
