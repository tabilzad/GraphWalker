package com.montecarlo.app.UI

import com.montecarlo.domain.Result
import tornadofx.App
import tornadofx.reloadStylesheetsOnFocus

/**
 * Created by FERMAT on 4/4/2018.
 */
class MyApp:App(MonteCarloView::class){
    init {
        reloadStylesheetsOnFocus()
    }
}