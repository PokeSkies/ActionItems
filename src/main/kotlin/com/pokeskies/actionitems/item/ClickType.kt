package com.pokeskies.actionitems.item

import com.google.gson.*
import com.pokeskies.actionitems.utils.Utils
import net.minecraft.util.StringRepresentable
import java.lang.reflect.Type

enum class ClickType(val identifier: String, val isLeft: Boolean, val isRight: Boolean, val shiftRequirement: ShiftRequirement) : StringRepresentable {
    LEFT_CLICK("left_click", true, false, ShiftRequirement.NOT_ALLOWED),
    SHIFT_LEFT_CLICK("shift_left_click", true, false, ShiftRequirement.REQUIRED),
    ANY_LEFT_CLICK("any_left_click", true, false, ShiftRequirement.IGNORED),

    RIGHT_CLICK("right_click", false, true, ShiftRequirement.NOT_ALLOWED),
    SHIFT_RIGHT_CLICK("shift_right_click", false, true, ShiftRequirement.REQUIRED),
    ANY_RIGHT_CLICK("any_right_click", false, true, ShiftRequirement.IGNORED),

    ANY_MAIN_CLICK("any_main_click", true, true, ShiftRequirement.NOT_ALLOWED),
    ANY_SHIFT_CLICK("any_shift_click", true, true, ShiftRequirement.REQUIRED),

    ANY("any", true, true, ShiftRequirement.IGNORED);

    override fun getSerializedName(): String {
        return this.identifier
    }

    companion object {
        fun valueOfAnyCase(name: String): ClickType? {
            for (type in values()) {
                if (name.equals(type.identifier, true)) return type
            }
            return null
        }
    }

    internal class ClickTypeAdaptor : JsonSerializer<ClickType>, JsonDeserializer<ClickType> {
        override fun serialize(src: ClickType, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
            return JsonPrimitive(src.identifier)
        }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ClickType {
            val click = valueOfAnyCase(json.asString)

            if (click == null) {
                Utils.printError("Could not deserialize Click Type '${json.asString}'!")
                return ANY
            }

            return click
        }
    }

    enum class ShiftRequirement {
        NOT_ALLOWED, IGNORED, REQUIRED;
    }
}
