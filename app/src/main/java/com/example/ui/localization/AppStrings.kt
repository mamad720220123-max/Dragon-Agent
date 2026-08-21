package com.example.ui.localization

object AppStrings {
    fun get(key: String, lang: String): String {
        val isFa = lang.equals("fa", ignoreCase = true)
        return when (key) {
            // Navigation
            "nav_code_agent" -> if (isFa) "ایجنت کد" else "Code Agent"
            "nav_chat" -> if (isFa) "چت" else "Chat"
            "nav_memory" -> if (isFa) "حافظه" else "Memory"
            "nav_api_keys" -> if (isFa) "کلیدها" else "API Keys"
            "nav_settings" -> if (isFa) "تنظیمات" else "Settings"

            // Common
            "app_title" -> "D agent"
            "app_name_full" -> if (isFa) "دراگون ایجنت" else "Dragon Agent"
            "save" -> if (isFa) "ذخیره" else "Save"
            "cancel" -> if (isFa) "انصراف" else "Cancel"
            "delete" -> if (isFa) "حذف" else "Delete"
            "edit" -> if (isFa) "ویرایش" else "Edit"
            "copy" -> if (isFa) "کپی" else "Copy"
            "copied" -> if (isFa) "کپی شد" else "Copied"
            "search" -> if (isFa) "جستجو..." else "Search..."
            "export" -> if (isFa) "خروجی" else "Export"
            "import" -> if (isFa) "ورودی" else "Import"
            "status_active" -> if (isFa) "فعال" else "Active"
            "loading" -> if (isFa) "در حال پردازش..." else "Thinking..."

            // Workspace
            "workspace_stream" -> if (isFa) "ایجنت" else "Agent"
            "workspace_editor" -> if (isFa) "کد" else "Code"
            "workspace_files" -> if (isFa) "فایل‌ها" else "Files"
            "workspace_preview" -> if (isFa) "پیش‌نمایش" else "Preview"
            "workspace_prompt_hint" -> if (isFa) "دستور یا تغییرات کد را بنویسید..." else "Ask Dragon Agent to write or edit code..."
            "workspace_export_zip" -> if (isFa) "خروجی ZIP" else "Export ZIP"
            "workspace_new_file" -> if (isFa) "فایل جدید" else "New File"
            "workspace_no_files" -> if (isFa) "فایلی وجود ندارد" else "No files yet"
            "workspace_saved" -> if (isFa) "تغییرات ذخیره شد" else "Saved successfully"

            // Chat Assistant
            "chat_title" -> if (isFa) "دستیار گفتگو" else "AI Chat Assistant"
            "chat_hint" -> if (isFa) "پیام خود را بنویسید..." else "Type a message..."
            "chat_clear" -> if (isFa) "پاک کردن چت" else "Clear Chat"
            "chat_code_preview" -> if (isFa) "اجرای کد" else "Run Code"
            "chat_copy_code" -> if (isFa) "کپی کد" else "Copy Code"
            "chat_save_file" -> if (isFa) "افزودن به پروژه" else "Add to Project"

            // Memory Bank
            "memory_title" -> if (isFa) "بانک حافظه هوشمند" else "Smart Memory Bank"
            "memory_add" -> if (isFa) "افزودن حافظه" else "Add Memory"
            "memory_count" -> if (isFa) "مورد ذخیره شده" else "memories stored"
            "memory_clear_all" -> if (isFa) "حذف همه حافظه‌ها" else "Clear All Memories"
            "memory_title_hint" -> if (isFa) "عنوان بخش حافظه" else "Section / Topic Title"
            "memory_content_hint" -> if (isFa) "دانش، قوانین کدنویسی یا نکاتی که مدل باید به خاطر بسپارد..." else "Knowledge, code rules, or notes for targeted retrieval..."
            "memory_category" -> if (isFa) "بخش حافظه" else "Memory Section"
            "memory_encrypted_badge" -> if (isFa) "رمزنگاری سخت‌افزاری" else "Hardware Encrypted"

            // API Providers
            "api_title" -> if (isFa) "سرویس‌های هوش مصنوعی" else "API Providers"
            "api_add" -> if (isFa) "افزودن کلید" else "Add Provider"
            "api_name" -> if (isFa) "نام سرویس" else "Provider Name"
            "api_base_url" -> if (isFa) "آدرس سرور (Base URL)" else "Base URL"
            "api_key" -> if (isFa) "کلید امنیتی (API Key)" else "API Key"
            "api_selected_model" -> if (isFa) "مدل انتخابی" else "Selected Model"
            "api_set_default" -> if (isFa) "تنظیم به عنوان پیش‌فرض" else "Set as Default"
            "api_default_badge" -> if (isFa) "پیش‌فرض" else "Default"
            "api_key_placeholder" -> if (isFa) "کلید وارد شده امن است" else "Key configured"

            // Settings & About
            "settings_title" -> if (isFa) "تنظیمات" else "Settings"
            "settings_appearance" -> if (isFa) "ظاهر و پوسته" else "Appearance & Theme"
            "settings_dark_mode" -> if (isFa) "حالت شب (دارک مود)" else "Dark Mode"
            "settings_language" -> if (isFa) "زبان برنامه" else "Language"
            "settings_lang_en" -> "English"
            "settings_lang_fa" -> "فارسی"
            "settings_support" -> if (isFa) "ارتباط با سازنده" else "Creator Support"
            "settings_telegram_desc" -> if (isFa) "ارسال نظرات، گزارش باگ و پیشنهادات در تلگرام:" else "For feedback, bug reports, and suggestions on Telegram:"
            "settings_open_telegram" -> if (isFa) "ارسال پیام به @mamad720220" else "Message @mamad720220"
            "settings_security" -> if (isFa) "امنیت و حافظه دستگاه" else "Security & Local Storage"
            "settings_security_desc" -> if (isFa) "تمام کلیدها، بخش‌های حافظه و کدهای پروژه به صورت رمزنگاری شده سخت‌افزاری روی دستگاه نگهداری می‌شوند." else "All API keys, memory segments, and project code are hardware-encrypted locally on device."
            "settings_terms" -> if (isFa) "شرایط و قوانین استفاده" else "Terms of Service"
            "settings_export_backup" -> if (isFa) "پشتیبان‌گیری و خروجی" else "Backup & Export"
            "settings_export_zip" -> if (isFa) "خروجی کل پروژه (ZIP)" else "Export Project (ZIP)"
            "settings_export_memory" -> if (isFa) "خروجی حافظه (JSON)" else "Export Memory (JSON)"
            "settings_version" -> if (isFa) "نسخه ۱.۰.۰" else "v1.0.0"

            else -> key
        }
    }
}
