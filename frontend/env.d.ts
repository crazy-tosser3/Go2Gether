/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_MOCK?: string
  readonly VITE_2GIS_KEY?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
