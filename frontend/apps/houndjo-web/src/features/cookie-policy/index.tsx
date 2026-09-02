export function CookiePolicy() {
    return (
        <div className="mx-auto max-w-3xl px-4 py-24 sm:px-6">
            <div className="mb-12">
                <h1 className="text-4xl font-bold tracking-tight text-gray-900 sm:text-5xl dark:text-white">
                    Cookie Policy
                </h1>
                <p className="mt-4 text-lg text-gray-600 dark:text-zinc-400">
                    Last updated: March 20, 2026
                </p>
            </div>

            <div className="prose prose-gray dark:prose-invert max-w-none">
                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        1. What Are Cookies
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        Cookies are small text files stored on your device when
                        you visit a website. They help us recognize your
                        browser, remember your preferences, and improve your
                        experience on our platform.
                    </p>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        2. Types of Cookies We Use
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        We use the following categories of cookies:
                    </p>
                    <ul className="mt-4 list-disc pl-6 text-gray-600 dark:text-zinc-400">
                        <li>
                            <strong className="text-gray-900 dark:text-white">
                                Strictly necessary cookies
                            </strong>{" "}
                            — Required for the platform to function. These
                            include session and authentication cookies.
                        </li>
                        <li>
                            <strong className="text-gray-900 dark:text-white">
                                Preference cookies
                            </strong>{" "}
                            — Store your settings such as language, theme, and
                            display preferences.
                        </li>
                        <li>
                            <strong className="text-gray-900 dark:text-white">
                                Analytics cookies
                            </strong>{" "}
                            — Help us understand how visitors interact with our
                            platform so we can improve it.
                        </li>
                    </ul>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        3. How We Use Cookies
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        We use cookies to keep you signed in, remember your
                        preferences across sessions, analyze usage patterns to
                        improve our services, and ensure the security and
                        integrity of your account.
                    </p>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        4. Third-Party Cookies
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        Some cookies may be set by third-party services that
                        appear on our pages. We do not control these cookies and
                        recommend reviewing the privacy policies of those third
                        parties.
                    </p>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        5. Managing Cookies
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        You can control and delete cookies through your browser
                        settings. Please note that disabling certain cookies may
                        affect the functionality of our platform. Refer to your
                        browser&apos;s help documentation for instructions on
                        managing cookies.
                    </p>
                </section>

                <section className="mb-10">
                    <h2 className="text-2xl font-semibold text-gray-900 dark:text-white">
                        6. Contact Us
                    </h2>
                    <p className="mt-4 text-gray-600 dark:text-zinc-400">
                        If you have any questions about our use of cookies,
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
