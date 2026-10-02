package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.TokenEntity
import com.example.data.model.VercelTeam
import com.example.ui.theme.*

@Composable
fun ScopeSelector(
    activeToken: TokenEntity?,
    teams: List<VercelTeam>,
    selectedTeam: VercelTeam?,
    onSelectPersonal: () -> Unit,
    onSelectTeam: (VercelTeam) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val currentName = selectedTeam?.name ?: activeToken?.username ?: "Personal"
    val avatarUrl = selectedTeam?.avatar ?: activeToken?.avatar

    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .testTag("scope_selector_trigger")
                .clip(RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            if (!avatarUrl.isNullOrBlank()) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = currentName,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .border(1.dp, VercelBorder, CircleShape)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(VercelGrayDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentName.take(1).uppercase(),
                        color = VercelWhite,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = currentName,
                color = VercelWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 120.dp)
            )

            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Switch Scope",
                tint = VercelGrayLight,
                modifier = Modifier.size(18.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(VercelSurfaceElevated)
                .border(1.dp, VercelBorder, RoundedCornerShape(8.dp))
                .widthIn(min = 200.dp)
        ) {
            Text(
                text = "PERSONAL ACCOUNT",
                color = VercelGray,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )

            DropdownMenuItem(
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = activeToken?.username ?: "Personal Account",
                            color = VercelWhite,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (selectedTeam == null) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = VercelBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                onClick = {
                    onSelectPersonal()
                    expanded = false
                },
                modifier = Modifier.testTag("select_personal_account")
            )

            if (teams.isNotEmpty()) {
                HorizontalDivider(color = VercelBorder, modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "TEAMS",
                    color = VercelGray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )

                teams.forEach { team ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = team.name,
                                    color = VercelWhite,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                if (selectedTeam?.id == team.id) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = VercelBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onSelectTeam(team)
                            expanded = false
                        },
                        modifier = Modifier.testTag("select_team_${team.slug}")
                    )
                }
            }
        }
    }
}
