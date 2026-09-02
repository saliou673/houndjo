"use client";

import { useGetAppConfigurations } from "@api-client";
import { SelectDropdown } from "@/components/select-dropdown";

type CurrencySelectProps = {
    value?: string;
    onValueChange?: (value: string) => void;
    disabled?: boolean;
    placeholder?: string;
};

export function CurrencySelect({
    value,
    onValueChange,
    disabled,
    placeholder = "Select currency",
}: CurrencySelectProps) {
    const { data, isPending } = useGetAppConfigurations({
        filter: { category: { equals: "CURRENCY" }, active: { equals: true } },
        pageable: { page: 0, size: 100 },
    });

    const items = data?.items?.map((item) => ({
        label: item.label && item.code ? `${item.label} (${item.code})` : (item.code ?? ""),
        value: item.code ?? "",
    }));

    return (
        <SelectDropdown
            defaultValue={value}
            onValueChange={onValueChange}
            isPending={isPending}
            items={items}
            disabled={disabled}
            placeholder={placeholder}
            isControlled
        />
    );
}
