package domain

/**
 * Created by FERMAT on 4/4/2018.
 */
data class Result(
        val lattice: String,
        val samples: String,
        val walk_length: String,
        val error: String,
        val conf_interval: String,
        val time: String
)