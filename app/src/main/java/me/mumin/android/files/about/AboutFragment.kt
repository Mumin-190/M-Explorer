/*
 * Copyright (c) 2018 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.about

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import me.mumin.android.files.databinding.AboutFragmentBinding
import me.mumin.android.files.ui.LicensesDialogFragment
import me.mumin.android.files.util.createViewIntent
import me.mumin.android.files.util.startActivitySafe

class AboutFragment : Fragment() {
    private lateinit var binding: AboutFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        AboutFragmentBinding.inflate(inflater, container, false)
            .also { binding = it }
            .root

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val activity = requireActivity() as AppCompatActivity
        activity.setSupportActionBar(binding.toolbar)
        activity.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.gitHubLayout.setOnClickListener { startActivitySafe(GITHUB_URI.createViewIntent()) }
        binding.licensesLayout.isVisible = false
//#ifdef NONFREE
        binding.privacyPolicyLayout.isVisible = true
        binding.privacyPolicyLayout.setOnClickListener {
            startActivitySafe(PRIVACY_POLICY_URI.createViewIntent())
        }
//#endif
        binding.authorNameLayout.setOnClickListener {
            startActivitySafe(AUTHOR_RESUME_URI.createViewIntent())
        }
        binding.authorGitHubLayout.setOnClickListener {
            startActivitySafe(AUTHOR_GITHUB_URI.createViewIntent())
        }
        binding.authorTwitterLayout.isVisible = false
    }

    companion object {
        private val GITHUB_URI = Uri.parse("https://github.com/Mumin-190/M-Exploreer.git")
        private val PRIVACY_POLICY_URI =
            Uri.parse("https://github.com/Mumin-190/M-Exploreer/blob/master/PRIVACY.md")
        private val AUTHOR_RESUME_URI = Uri.parse("https://github.com/Mumin-190")
        private val AUTHOR_GITHUB_URI = Uri.parse("https://github.com/Mumin-190")
    }
}
