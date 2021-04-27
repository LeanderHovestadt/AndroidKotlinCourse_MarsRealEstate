package com.udacity.asteroidradar

import android.provider.Settings.Global.getString
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

@BindingAdapter("statusIcon")
fun ImageView.bindAsteroidStatusImage(isHazardous: Boolean) {
    if (isHazardous) {
        setImageResource(R.drawable.ic_status_potentially_hazardous)
    } else {
        setImageResource(R.drawable.ic_status_normal)
    }
}

@BindingAdapter("asteroidStatusImage")
fun ImageView.bindDetailsStatusImage(isHazardous: Boolean) {
    if (isHazardous) {
        setImageResource(R.drawable.asteroid_hazardous)
    } else {
        setImageResource(R.drawable.asteroid_safe)
    }
}

@BindingAdapter("astronomicalUnitText")
fun TextView.bindTextViewToAstronomicalUnit(number: Double) {
    text = String.format(context.getString(R.string.astronomical_unit_format), number)
}

@BindingAdapter("kmUnitText")
fun TextView.bindTextViewToKmUnit(number: Double) {
    text = String.format(context.getString(R.string.km_unit_format), number)
}

@BindingAdapter("velocityText")
fun TextView.bindTextViewToDisplayVelocity(number: Double) {
    text = String.format(context.getString(R.string.km_s_unit_format), number)
}

@BindingAdapter("dateText")
fun TextView.bindTextViewToDisplayDate(date: Date){
    val dateFormat = SimpleDateFormat(Constants.API_QUERY_DATE_FORMAT, Locale.getDefault())
    text = dateFormat.format(date)
}

@BindingAdapter("asteroidStatusImageContentDescription")
fun ImageView.bindContentDescriptionToAsteroidStatus(isHazardous: Boolean) {
    if (isHazardous) {
        contentDescription = context.getString(R.string.potentially_hazardous_asteroid_status)
    } else {
        contentDescription = context.getString(R.string.not_hazardous_asteroid_status)
    }
}

@BindingAdapter("imageOfTheDay")
fun ImageView.bindImageOfTheDay(url: String?)
{
    Timber.i("bindImageOfTheDay")
    url?.let {
        val imgUri = it.toUri().buildUpon().scheme("https").build()
        Timber.i("imgUri: ${imgUri.encodedPath}")
        Glide.with(context)
            .load(imgUri)
            .apply(
                RequestOptions()
                .placeholder(R.drawable.placeholder_picture_of_day)
                .error(R.drawable.ic_broken_image))
            .into(this)
    }
}

@BindingAdapter("imageOfTheDayContentDescription")
fun ImageView.bindImageOfTheDayContentDescription(content: String?)
{
    if (content != null) {
        contentDescription = String.format(context.getString(R.string.nasa_picture_of_day_content_description_format), content)
    }
    else
    {
        contentDescription = context.getString(R.string.this_is_nasa_s_picture_of_day_showing_nothing_yet)
    }

}