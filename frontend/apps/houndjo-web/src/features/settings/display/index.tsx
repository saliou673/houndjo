import { useTranslations } from "next-intl";
import { ContentSection } from "../components/content-section";
import { DisplayForm } from "./display-form";

export function SettingsDisplay() {
    const t = useTranslations("SettingsDisplay");

    return (
        <ContentSection
            title={t("sectionTitle")}
            desc={t("sectionDescription")}
        >
            <DisplayForm />
        </ContentSection>
    );
}
