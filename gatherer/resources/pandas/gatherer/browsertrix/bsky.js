/**
 * Fetches all the JavaScript chunks from the Bluesky app.
 * Bsky puts different languages in different chunks (including English variants like UK English). Without this, if the
 * replay browser has a different primary language setting to the crawler, it will fail to replay.
 */
class BskyBehavior {
    static id = 'bsky';
    static runInIframes = false;

    static init() {
        return {
           state: {}
        };
    }

    static isMatch() {
        return window.location.host.endsWith('bsky.app');
    }

    extractChunkFileNames(code) {
        const mappingRegex = /"static\/js\/"\s*\+\s*e\s*\+\s*"\."\s*\+\s*\{([\s\S]+?)}\s*\[\s*e]\s*\+\s*"\.chunk\.js"/;
        const mapMatch = code.match(mappingRegex);
        if (!mapMatch) return [];  // nothing found

        const mapBody = mapMatch[1];

        // pull out each id:"hash" pair
        const pairRegex = /(\d+):"([0-9a-fA-F]+)"/g;
        const chunks = [];
        while (true) {
            const match = pairRegex.exec(mapBody);
            if (match === null) break;
            const [ , id, hash ] = match;
            chunks.push(`/static/js/${id}.${hash}.chunk.js`);
        }

        return chunks;
    }

    // force load all the JavaScript chunks
    async* run() {
        yield {msg:"Loading chunk.js files"};
        let mainScript = document.querySelector('script[src*="/static/js/main."]');
        if (!mainScript) {
            yield {msg:"No main.js script found"};
            return;
        }
        const mainScriptUrl = mainScript.src;
        const chunkFiles = await fetch(mainScriptUrl)
            .then(r => r.text())
            .then(text => {
                let chunkFiles = this.extractChunkFileNames(text);
                for (let chunkFile of chunkFiles) {
                    fetch(new URL(chunkFile, mainScriptUrl));
                }
                return chunkFiles;
            });
        yield {msg:"Loaded chunk.js files", chunkFiles};
    }
}