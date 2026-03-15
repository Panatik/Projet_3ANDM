package com.example.projet_3andm

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun CardItem(title: String, image: Painter){
    Box(Modifier.width(265.dp).background(color = Color.White)){
        Column() {
            Image(
                painter = image,
                contentDescription = null
            )
            Text(
                text = title,
                modifier = Modifier.padding(all = 16.dp),
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Preview()
@Composable
fun Preview(){
    CardItem(title = "Je Lorem le Ipsum", image = painterResource(R.drawable.ip5xtp1769779958))
}