import { useEffect, useRef } from "react";
import { usePathname, useSearchParams } from "next/navigation";
import LoadingBar, { type LoadingBarRef } from "react-top-loading-bar";

export function NavigationProgress() {
    const ref = useRef<LoadingBarRef>(null);
    const pathname = usePathname();
    const searchParams = useSearchParams();
    const routeKey = `${pathname}?${searchParams.toString()}`;

    useEffect(() => {
        ref.current?.continuousStart();
        const timeout = setTimeout(() => ref.current?.complete(), 200);
        return () => clearTimeout(timeout);
    }, [routeKey]);

    return (
        <LoadingBar
            color="var(--muted-foreground)"
            ref={ref}
            shadow={true}
            height={2}
        />
    );
}
