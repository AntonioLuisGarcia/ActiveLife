package edu.tfc.activelife.ui.fragments

import android.os.Bundle
import androidx.navigation.NavArgs
import kotlin.String
import kotlin.jvm.JvmStatic

public data class FragmentTwoArgs(
    public val routineId: String = "defaultId"
) : NavArgs {
    public fun toBundle(): Bundle {
        val result = Bundle()
        result.putString("routineId", this.routineId)
        return result
    }

    public companion object {
        @JvmStatic
        public fun fromBundle(bundle: Bundle): FragmentTwoArgs {
            bundle.setClassLoader(FragmentTwoArgs::class.java.classLoader)
            val __routineId : String?
            if (bundle.containsKey("routineId")) {
                __routineId = bundle.getString("routineId")
                if (__routineId == null) {
                    throw IllegalArgumentException("Argument \"routineId\" is marked as non-null but was passed a null value.")
                }
            } else {
                __routineId = "defaultId"
            }
            return FragmentTwoArgs(__routineId)
        }
    }
}
