export function Privacy() {
    return (
        <div className="mx-auto max-w-3xl px-4 py-24 sm:px-6">
            <div className="mb-12">
                <h1 className="text-4xl font-bold tracking-tight text-gray-900 sm:text-5xl dark:text-white">
                    Privacy Policy
                </h1>
                <p className="mt-4 text-lg text-gray-600 dark:text-zinc-400">
                    Last updated: March 20, 2026
                </p>
            </div>

            <div className="prose prose-gray dark:prose-invert max-w-none">
                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        1. Information We Collect
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        We collect information you provide directly to us, such
                        as when you create an account, use our services, or
                        contact us for support. This includes:
                    </p>
                    <ul className="mt-4 list-disc pl-6 text-gray-600 dark:text-zinc-400">
                        <li>Name and email address</li>
                        <li>Account credentials</li>
                        <li>Billing and payment information</li>
                        <li>
                            Usage data and activity logs within our platform
                        </li>
                    </ul>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        2. How We Use Your Information
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        We use the information we collect to operate, maintain,
                        and improve our services, process transactions, send
                        transactional and promotional communications, and comply
                        with legal obligations.
                    </p>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        3. Information Sharing
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        We do not sell, trade, or rent your personal information
                        to third parties. We may share your information with
                        trusted service providers who assist us in operating our
                        platform, subject to confidentiality agreements.
                    </p>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        4. Data Retention
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        We retain your personal information for as long as your
                        account is active or as needed to provide you services.
                        You may request deletion of your data at any time by
                        contacting us.
                    </p>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        5. Security
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        We implement industry-standard security measures to
                        protect your personal information against unauthorized
                        access, alteration, disclosure, or destruction.
                    </p>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        6. Contact Us
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        If you have any questions about this Privacy Policy,
                        please contact us at{" "}
                        <a
                            href="mailto:privacy@houndjo.com"
                            className="text-violet-600 hover:underline dark:text-violet-400"
                        >
                            privacy@houndjo.com
                        </a>
                        .
                    </p>
                </section>
            </div>
        </div>
    );
}
