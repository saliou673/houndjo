"use client";

import { useEffect } from "react";
import { Button } from "@/components/ui/button";

type GlobalErrorProps = {
    error: Error & { digest?: string };
    reset: () => void;
};

export default function GlobalError({ error, reset }: GlobalErrorProps) {
    useEffect(() => {
        // eslint-disable-next-line no-console
        console.error(error);
    }, [error]);

    return (
        <html lang="en">
            <body>
                <div className="h-svh">
                    <div className="m-auto flex h-full w-full flex-col items-center justify-center gap-2">
                        <h1 className="text-[7rem] leading-tight font-bold">
                            500
                        </h1>
                        <span className="font-medium">
                            Oops! Something went wrong {`:')`}
                        </span>
                        <p className="text-center text-muted-foreground">
                            We apologize for the inconvenience. <br /> Please
                            try again later.
                        </p>
                        <div className="mt-6 flex gap-4">
                            <Button variant="outline" onClick={reset}>
                                Try again
                            </Button>
                            <Button
                                onClick={() => (window.location.href = "/")}
                            >
                                Back to Home
                            </Button>
                        </div>
                    </div>
                </div>
            </body>
        </html>
    );
}
