/*
 * Mozilla Readability.js - Simplified version for SereneSec
 * Based on: https://github.com/mozilla/readability
 * License: Apache-2.0
 * 
 * This is a minimal extraction for article parsing.
 * Full library: https://unpkg.com/@pocketbase/readability/dist/Readability.js
 */

var Readability = function(doc, options) {
    this._doc = doc;
    this._articleTitle = "";
    this._articleByline = "";
    this._articleContent = null;
    
    this.DEFAULT_TAGS_TO_SCORE = "section,h2,h3,h4,h5,h6,p,td,pre".toUpperCase().split(",");
    this.UNLIKELY_CANDIDATES = /banner|combx|comment|community|disqus|extra|foot|header|menu|modal|nav|remark|rss|share|shoutbox|sidebar|skyscraper|sponsor|ad-|widget|social/i;
    this.OK_MAYBE_ITS_A_CANDIDATE = /article|body|content|entry|main|page|post|text|blog|story/i;
};

Readability.prototype = {
    parse: function() {
        var articleContent = this._grabArticle();
        if (!articleContent) {
            return null;
        }
        
        return {
            title: this._getArticleTitle(),
            byline: this._articleByline,
            content: articleContent.innerHTML,
            textContent: articleContent.textContent,
            length: articleContent.textContent.length
        };
    },
    
    _getArticleTitle: function() {
        var title = this._doc.title;
        var h1 = this._doc.querySelector("h1");
        if (h1) {
            var h1Text = h1.textContent.trim();
            if (h1Text.length > 15) {
                title = h1Text;
            }
        }
        return title;
    },
    
    _grabArticle: function() {
        var article = this._doc.querySelector("article");
        if (article) {
            return this._prepareArticle(article.cloneNode(true));
        }
        
        var main = this._doc.querySelector("[role=main], main, .post-content, .article-content, .entry-content");
        if (main) {
            return this._prepareArticle(main.cloneNode(true));
        }
        
        var candidates = this._doc.querySelectorAll("div, section");
        var best = null;
        var bestScore = 0;
        
        for (var i = 0; i < candidates.length; i++) {
            var candidate = candidates[i];
            var score = this._scoreNode(candidate);
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        
        if (best) {
            return this._prepareArticle(best.cloneNode(true));
        }
        
        return this._doc.body.cloneNode(true);
    },
    
    _scoreNode: function(node) {
        var score = 0;
        var className = node.className || "";
        var id = node.id || "";
        
        if (this.UNLIKELY_CANDIDATES.test(className + " " + id)) {
            score -= 25;
        }
        if (this.OK_MAYBE_ITS_A_CANDIDATE.test(className + " " + id)) {
            score += 25;
        }
        
        var paragraphs = node.querySelectorAll("p");
        score += paragraphs.length * 3;
        
        var links = node.querySelectorAll("a");
        var textLength = node.textContent.length;
        var linkLength = 0;
        for (var i = 0; i < links.length; i++) {
            linkLength += links[i].textContent.length;
        }
        if (textLength > 0) {
            var linkDensity = linkLength / textLength;
            if (linkDensity > 0.5) {
                score -= 50;
            }
        }
        
        return score;
    },
    
    _prepareArticle: function(article) {
        var tagsToRemove = ["script", "style", "noscript", "iframe", "form", "nav", "aside", "footer", "header"];
        for (var i = 0; i < tagsToRemove.length; i++) {
            var elements = article.querySelectorAll(tagsToRemove[i]);
            for (var j = elements.length - 1; j >= 0; j--) {
                elements[j].parentNode.removeChild(elements[j]);
            }
        }
        
        var elementsToRemove = article.querySelectorAll("[class*='comment'], [class*='share'], [class*='social'], [class*='related'], [class*='sidebar'], [class*='widget'], [class*='ad-'], [id*='comment'], [id*='share'], [id*='social']");
        for (var k = elementsToRemove.length - 1; k >= 0; k--) {
            elementsToRemove[k].parentNode.removeChild(elementsToRemove[k]);
        }
        
        return article;
    }
};

if (typeof module !== "undefined") {
    module.exports = Readability;
}
