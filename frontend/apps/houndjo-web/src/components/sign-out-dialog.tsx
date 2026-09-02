import { useQueryClient } from "@tanstack/react-query";
import { useLogout } from "@api-client";
import { signOut } from "next-auth/react";
import { usePathname, useSearchParams } from "next/navigation";
import { toast } from "sonner";
import { ConfirmDialog } from "@/components/confirm-dialog";

interface SignOutDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function SignOutDialog({ open, onOpenChange }: SignOutDialogProps) {
    const pathname = usePathname();
    const searchParams = useSearchParams();
    const queryClient = useQueryClient();
    const { mutateAsync: logout, isPending: isSigningOut } = useLogout({
        mutation: {
            onSuccess: async () => {
                queryClient.clear();
                const query = searchParams.toString();
                const currentPath = query ? `${pathname}?${query}` : pathname;
                await signOut({
                    redirect: true,
                    callbackUrl: `/sign-in?redirect=${encodeURIComponent(currentPath)}`,
                });
            },
            onError: () =>
                toast.error("Failed to sign out. Please try again later."),
        },
    });

    const handleSignOut = async () => await logout({});

    return (
        <ConfirmDialog
            open={open}
            onOpenChange={onOpenChange}
            title="Sign out"
            desc="Are you sure you want to sign out? You will need to sign in again to access your account."
            confirmText="Sign out"
            destructive
            handleConfirm={handleSignOut}
            isLoading={isSigningOut}
            className="sm:max-w-sm"
        />
    );
}
