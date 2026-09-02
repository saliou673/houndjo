export function Terms() {
    return (
        <div className="mx-auto max-w-3xl px-4 py-24 sm:px-6">
            <div className="mb-12">
                <h1 className="text-4xl font-bold tracking-tight text-gray-900 sm:text-5xl dark:text-white">
                    Terms of Service
                </h1>
                <p className="mt-4 text-lg text-gray-600 dark:text-zinc-400">
                    Last updated: March 20, 2026
                </p>
            </div>

            <div className="prose prose-gray dark:prose-invert max-w-none">
                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        1. Acceptance of Terms
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        By accessing or using Houndjo, you agree to be bound
                        by these Terms of Service. If you do not agree to these
                        terms, please do not use our services.
                    </p>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        2. Use of Services
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        You agree to use our services only for lawful purposes
                        and in accordance with these Terms. You must not use our
                        services to:
                    </p>
                    <ul className="mt-4 list-disc pl-6 text-gray-600 dark:text-zinc-400">
                        <li>Violate any applicable laws or regulations</li>
                        <li>Infringe on the rights of others</li>
                        <li>
                            Transmit harmful, offensive, or disruptive content
                        </li>
                        <li>
                            Attempt to gain unauthorized access to our systems
                        </li>
                    </ul>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        3. Accounts
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        You are responsible for maintaining the security of your
                        account credentials and for all activity that occurs
                        under your account. Notify us immediately of any
                        unauthorized use.
                    </p>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        4. Intellectual Property
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        Houndjo and its original content, features, and
                        functionality are owned by Houndjo and are protected
                        by international copyright, trademark, and other
                        intellectual property laws.
                    </p>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        5. Termination
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        We reserve the right to suspend or terminate your
                        account at our sole discretion, without notice, for
                        conduct that we believe violates these Terms or is
                        harmful to other users, us, or third parties.
                    </p>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        6. Limitation of Liability
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        To the maximum extent permitted by law, Houndjo shall
                        not be liable for any indirect, incidental, special,
                        consequential, or punitive damages resulting from your
                        use of or inability to use our services.
                    </p>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        7. Contact Us
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        If you have any questions about these Terms, please
                        contact us at{" "}
                        <a
                            href="mailto:legal@houndjo.com"
                            className="text-violet-600 hover:underline dark:text-violet-400"
                        >
                            legal@houndjo.com
                        </a>
                        .
                    </p>
                </section>
            </div>
        </div>
    );
}
