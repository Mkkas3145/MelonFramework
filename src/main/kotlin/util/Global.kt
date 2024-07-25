package util

import getUUID

class Global {
    companion object {
        private val values = mutableMapOf<String, Any>()

        @Suppress("UNCHECKED_CAST")
        fun <T> getValue(key: String): T? {
            return values[key] as? T
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> getValueOrDefault(key: String, default: T): T? {
            return values.getOrDefault(key, default) as? T
        }

        fun setValue(key: String, value: Any): Companion {
            values[key] = value
            return this
        }

        fun getProfile(): Profile {
            val map = getValue<Map<String, Any>>(getUUID().toString())
            val profile = map?.getOrDefault("profile", Profile.Local).toString()
            if (profile.equals("dev", true)) {
                return Profile.Dev
            } else if (profile.equals("stage", true)) {
                return Profile.Stage
            } else if (profile.equals("prod", true)) {
                return Profile.Prod
            }
            return Profile.Local
        }

        fun setProfile(profile: Profile): Companion {
            val map = getValueOrDefault<MutableMap<String, Any>>(getUUID().toString(), mutableMapOf())
            map!!["profile"] = profile
            setValue(getUUID().toString(), map)
            println(values)
            return this
        }
    }
}