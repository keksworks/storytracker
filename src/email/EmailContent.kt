package email

import klite.html.unaryPlus
import klite.i18n.Lang.translate
import org.intellij.lang.annotations.Language
import java.net.URI

class EmailContent(lang: String, labelKey: String, substitutions: Map<String, String> = emptyMap(), actionUrl: URI? = null):
  klite.smtp.EmailContent(lang, labelKey, substitutions, actionUrl) {
  override val body get() = translate(lang, "emails.$labelKey.bodyHtml", substitutions)

  @Language("html")
  override fun contentHtml() = """
<div role="article" aria-roledescription="email" lang="$lang" style="background-color: rgb(243, 244, 246); padding: 1em">
  <table role="presentation" style="width: 94%; max-width: 480px; margin: 0 auto">
    <tr>
      <td style="padding: 2em; background: white; color: rgb(17, 24, 39)">
        <div>
          <img height="32" alt="Stampy.Guru" src="https://stampy.guru/img/icon.png">
        </div>
        <h1 style="margin: 1em 0; font-size: 1.625em; line-height: 1.25; font-weight: bold">${+subject}</h1>
        <div style="margin-bottom: 1em; white-space: pre-line">$body</div>
        ${actionUrl?.let {"""
          <a href="$it" style="background: rgb(17, 24, 39); font-weight: bold; text-decoration: none; text-align: center; padding: 1em 2em; color: white; border-radius: 4px; margin-bottom: 1em; display: block">
            $actionLabel
          </a>
        """} ?: ""}
        <p style="margin: 0; font-size: 0.8em; color: rgb(107, 114, 128); text-align: center">
          ${translate(lang, "legal.name")}
        </p>
      </td>
    </tr>
  </table>
</div>
"""
}
