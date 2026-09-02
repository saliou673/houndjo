import { defineConfig } from "@kubb/core";
import { pluginClient } from "@kubb/plugin-client";
import { pluginOas } from "@kubb/plugin-oas";
import { pluginReactQuery } from "@kubb/plugin-react-query";
import { pluginTs } from "@kubb/plugin-ts";

export default defineConfig({
    input: {
        path: "http://localhost:8080/api/docs",
    },
    output: {
        path: "./src/gen",
        clean: true,
    },
    plugins: [
        pluginOas(),
        pluginTs(),
        // Group by OpenAPI tag so generic operationIds like create/delete
        // are scoped per domain and avoid numeric suffixes in most cases.
        // Example: auth/create.ts, user/create.ts instead of create1.ts.
        pluginClient({
            output: {
                path: "./client",
            },
            group: {
                type: "tag",
                name: ({ group }) =>
                    group
                        .replace(/[^a-zA-Z0-9]+/g, " ")
                        .trim()
                        .replace(/\s+/g, "-")
                        .toLowerCase(),
            },
        }),
        pluginReactQuery({
            output: {
                path: "./react-query",
            },
            group: {
                type: "tag",
                name: ({ group }) =>
                    group
                        .replace(/[^a-zA-Z0-9]+/g, " ")
                        .trim()
                        .replace(/\s+/g, "-")
                        .toLowerCase(),
            },
        }),
    ],
});
