package de.tudarmstadt.iptk.foxtrot.vivacoronia.infectionStatus

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import de.tudarmstadt.iptk.foxtrot.vivacoronia.R

val INFECTED_STATE = intArrayOf(R.attr.infected_state)
val RECOVERED_STATE = intArrayOf(R.attr.recovered_state)

class InfectionStateLinearLayout: LinearLayout {
    enum class InfectionState {
        UNKNOWN, RECOVERED, INFECTED
    }

    var infectionState = InfectionState.UNKNOWN
        set(value) {
            field = value
            refreshDrawableState()
        }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val state = super.onCreateDrawableState(extraSpace + 1) // 1 because we potentially add 1 extra state: either recovered or infected (or none at all)
        if (infectionState == InfectionState.INFECTED)
            mergeDrawableStates(state, INFECTED_STATE)
        else if (infectionState == InfectionState.RECOVERED)
            mergeDrawableStates(state, RECOVERED_STATE)
        return state
    }
}
